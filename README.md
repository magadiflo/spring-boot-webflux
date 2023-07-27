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

    /* getters and setters */
}
````

````java
public interface IProductRepository extends ReactiveMongoRepository<Product, String> {
}
````
