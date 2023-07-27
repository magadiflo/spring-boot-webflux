# Sección 3: Introducción programación reactiva con Spring WebFlux

Creación del proyecto

![creacion-proyecto](./assets/creacion-proyecto.png)

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
