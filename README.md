# Sección 3: Introducción programación reactiva con Spring WebFlux

Creación del proyecto

![creacion-proyecto](./assets/creacion-proyecto.png)

## Programación Reactiva

La programación reactiva es un paradigma de programación orientado al flujo de datos (streams) y la propagación del
cambio, todo de forma asíncrona.

Esto quiere decir que la programación reactiva se sustenta en el patrón de diseño Observer, donde se tiene un Publisher
y uno o más Suscribers que reciben notificaciones cuando el Publisher emite nuevos datos.

En la programación reactiva, **el Publisher es el que se encarga de emitir el flujo de datos** y **propaga el cambio
(notifica) a los Suscribers.**

Por lo tanto, podemos decir que la programación reactiva se basa en 3 conceptos clave:

- **Publisher:** También llamados **Observables.** Estos objetos son los que emiten el flujo de datos.
- **Suscriber:** También llamados **Observers.** Estos objetos son a los que se les notifican los cambios en el flujo de
  datos que emite el Publisher
- **Schedulers:** Es el componente que administra la concurrencia. Se encarga de indicarle a los Publishers y Suscribers
  en que thread deben ejecutarse.

## Agregando clases del modelo document y repository

Cuando trabajamos con SQL creábamos entities mapeados a tablas de una base de datos relacional. Ahora, como estamos
trabajando con MongoDB, que **es una base de datos NoSQL**, trabajaremos con **colecciones y no con tablas.**

Como primer paso, crearemos la colección a productos y su interface de repositorio:

````java

@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private Double price;
    private LocalDateTime createAt;

    public Product() {
    }

    public Product(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    /* getters and setters */
}
````

````java
public interface IProductRepository extends ReactiveMongoRepository<Product, String> {
}
````

## Importando datos de ejemplo y el operador flatMap

Si recordamos, cuando creábamos nuestra aplicación normal, basada en servlets, podíamos crear un archivo **import.sql**
para poder insertar en automático nuestros datos, pero como ahora estamos trabajando con una base de datos **NoSQL**,
debemos buscar otra manera de poder insertar datos de prueba. Una forma de hacerlo es de manera programática, veamos
cómo:

````java

@SpringBootApplication
public class SpringBootWebfluxApplication {

    private final static Logger LOG = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
    private final IProductRepository productRepository;

    public SpringBootWebfluxApplication(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /* main method omitted */

    @Bean
    public CommandLineRunner run() {
        return args -> {
            Flux.just(
                            new Product("Tv LG 70'", 3609.40),
                            new Product("Sony Cámara HD", 680.60),
                            new Product("Bicicleta Monteñera", 1800.60),
                            new Product("Monitor 27' LG", 750.00),
                            new Product("Teclado Micronics", 17.00),
                            new Product("Celular Huawey", 900.00),
                            new Product("Interruptor simple", 6.00),
                            new Product("Pintura Satinado", 78.00),
                            new Product("Pintura Base", 10.00),
                            new Product("Sillón 3 piezas", 10.00),
                            new Product("Separador para TV", 10.00),
                            new Product("Armario 2 puertas", 910.00),
                            new Product("Colchón Medallón 2 plazas", 710.00),
                            new Product("Silla de oficina", 540.00)
                    )
                    .flatMap(this.productRepository::save)
                    .subscribe(
                            product -> LOG.info("Insertado: {}, {}", product.getId(), product.getName()),
                            error -> LOG.error("Error al insertar: {}", error.getMessage()),
                            () -> LOG.info("¡Inserción completada!")
                    );

        };
    }
}
````

En el código anterior usamos la interfaz funcional **CommandLineRunner** anotándolo con **@Bean**. De esta manera
nuestra aplicación se ejecutará una vez arranque el mismo.

### flatMap

Estamos usando el flatMap, ya que dentro de él usamos el ``this.productRepository.save(product)`` para mandar a guardar
el **Product** emitido. Este **save()** nos retorna un ``Mono<Product>``. Finalmente, cuando hacemos el **subscribe()**
lo que recibimos es el model **product** guardado.

Ahora, corremos la aplicación y este será el resultado obtenido en consola:

![insertando-productos-default](./assets/insertando-productos-default.png)

Si revisamos la base de datos de MongoDB veremos que **por defecto se insertaron los productos en la base de datos
Test**, es decir, hasta este punto no hemos configurado ninguna base de datos, pero por defecto al ejecutar la
aplicación utilizó la base de datos **test** para insertar nuestros datos:

![insertando-datos-default-mongodb](./assets/insertando-datos-default-mongodb.png)

## Eliminando colección de productos (drop collection)

En la sección anterior implementamos el método **run()** de la interfaz funcional **CommandLineRunner** para poder
poblar la base de datos de MongoDB con valores iniciales. Ahora, el problema es que cada vez que levantamos la
aplicación los datos se van registrando, es decir se van duplicando los datos. Entonces para evitar eso,
lo que haremos será que antes de poblar la base de datos con registros, primero eliminaremos todo lo que haya en la
colección, de esa manera tendremos siempre el mismo conjunto de datos.

````java

@SpringBootApplication
public class SpringBootWebfluxApplication {

    /* Other code */
    private final ReactiveMongoTemplate reactiveMongoTemplate; // (1)

    public SpringBootWebfluxApplication(IProductRepository productRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productRepository = productRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            this.reactiveMongoTemplate.dropCollection("products").subscribe(); // (2)

            /* Código que puebla la colección de products */
        };
    }
}
````

**DONDE**

- **(1)** aplicamos inyección de dependencia vía constructor para el **ReactiveMongoTemplate**. Con esta clase podemos
  eliminar directamente la colección **products**.
- **(2)** usando el **ReactiveMongoTemplate** llamamos al método **dropCollection()** para eliminar la colección
  **products**.

### ReactiveMongoTemplate

ReactiveMongoTemplate es una clase auxiliar que nos ayuda a incrementar nuestras consultas a la BD mediante las
MongoOperations de manera reactiva.

Implementación principal de ReactiveMongoOperations. Simplifica el uso de Reactive MongoDB y ayuda a evitar errores
comunes. Ejecuta el flujo de trabajo central de MongoDB, dejando el código de la aplicación para proporcionar documentos
y extraer resultados. Esta clase ejecuta consultas o actualizaciones de BSON, iniciando la iteración sobre FindPublisher
y capturando las excepciones de MongoDB y traduciéndolas a la jerarquía de excepción genérica y más informativa definida
en el paquete org.springframework.dao.

## Cambiando el nombre de la base de datos y agregando la fecha createAt

Por defecto, cuando arrancamos la aplicación, los datos se van a poblar en la base de datos **test de mongoDB**, pero
ahora nosotros crearemos nuestra propia base de datos llamada **db_spring_boot_webflux**.

En el **application.properties** creamos la cadena de conexión a la base de datos de MongoDB agregándole el nombre que
le daremos a nuestra base de datos.
****

````properties
spring.data.mongodb.uri=mongodb://localhost:27017/db_spring_boot_webflux
````

Como nota final, si la base de datos **db_spring_boot_webflux** no está creada, al arrancar la aplicación, mongoDB
la va a crear por nosotros.

Ahora, con respecto a agregar fecha a nuestros registros, podemos usar el operador **flatMap()** para que en su interior
poblemos con la fecha actual a cada producto que va pasando:

````java

@SpringBootApplication
public class SpringBootWebfluxApplication {
    @Bean
    public CommandLineRunner run() {
        return args -> {
            this.reactiveMongoTemplate.dropCollection("products").subscribe();
            Flux.just(/* data */)
                    .flatMap(product -> {
                        product.setCreateAt(LocalDateTime.now()); //<-- asignando fecha actual a cada producto
                        return this.productRepository.save(product);
                    });
            /* other code */
        };
    }
}
````

**IMPORTANTE**

> **MongoDB almacena las horas en UTC de forma predeterminada y convierte cualquier representación de la hora local a
> este formato.** Las aplicaciones que deben operar o informar sobre algún valor de hora local sin modificar pueden
> almacenar la zona horaria junto con la marca de tiempo UTC y calcular la hora local original en su lógica de
> aplicación.
>
> [FUENTE: MongoDB](https://www.mongodb.com/docs/manual/tutorial/model-iot-data/)

Ahora, en nuestra aplicación de **Spring Boot**, nuestra base de datos **MongoDB** también está almacenando nuestra zona
de horario local en **UTC**, pero cuando lo recuperamos con las consultas de un **MongoRepository**, realiza la
conversión automáticamente y recupera nuestra fecha y hora correcta.

![time-zone.png](./assets/time-zone.png)

## Creando controlador y vista reactivos

Crearemos un controlador del tipo **@Controller**, ya que nuestras vistas serán renderizadas en plantillas html usando
**thymeleaf**.

````java

@Controller
@RequestMapping(path = {"/", "/products"})
public class ProductController {
    private final IProductRepository productRepository;

    public ProductController(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping(path = {"/", "/list"})
    public String list(Model model) {
        Flux<Product> productFlux = this.productRepository.findAll();
        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list";
    }
}
````

Como observamos en el código anterior, **en ningún momento estamos haciendo el subscribe() del flux de productos para
empezar a recibir los datos.** Nosotros no debemos preocuparnos por eso, **quien se subscribirá al flux será la vista de
Thymeleaf**, él lo hará por nosotros.

El html usando Thymeleaf sería el siguiente (qu)

````html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${title}"></title>
    <!-- link to bootstrap -->
</head>
<body>
<main class="container">
    <h1 th:text="${title}" class="my-3 border-bottom"></h1>
    <table class="table table-striped table-hover">
        <thead>
        <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Precio</th>
            <th>Creación</th>
        </tr>
        </thead>
        <tbody>
        <tr th:each="product: ${products}">
            <td th:text="${product.id}"></td>
            <td th:text="${product.name}"></td>
            <td th:text="${product.price}"></td>
            <td th:text="${product.createAt}"></td>
        </tr>
        </tbody>
    </table>
</main>
</body>
</html>
````

Finalmente, al ejecutar la aplicación veremos el siguiente resultado:

![listado-de-productos-thymeleaf](./assets/listado-de-productos-thymeleaf.png)

## Subscribiendo otro Observador y modificando el Stream Reactivo

**(1)** Crearemos un nuevo **observer (suscriber)** que se subscribirá al **observable (publisher)** definido en la
variable **productFlux** para mostrar los datos en consola, este sería nuestro segundo subscriptor, el primero es
nuestra vista de Thymeleaf que también se encuentra subscrito por defecto a dicho observable.

Además, utilizamos el operador **map()** solo para hacer una modificación a cada elemento del flujo:

````java

@Controller
@RequestMapping(path = {"/", "/products"})
public class ProductController {
    private final static Logger LOG = LoggerFactory.getLogger(ProductController.class);
    /* omitted code */

    @GetMapping(path = {"/", "/list"})
    public String list(Model model) {
        Flux<Product> productFlux = this.productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                });

        productFlux.subscribe(product -> LOG.info(product.getName())); // (1)

        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list";
    }
}
````
