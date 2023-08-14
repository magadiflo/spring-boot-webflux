package com.magadiflo.webflux.app;

import com.magadiflo.webflux.app.models.documents.Category;
import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.services.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@SpringBootApplication
public class SpringBootWebfluxApplication {

    private final static Logger LOG = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
    private final IProductService productService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public SpringBootWebfluxApplication(IProductService productService, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productService = productService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            this.reactiveMongoTemplate.dropCollection("products").subscribe();
            this.reactiveMongoTemplate.dropCollection("categories").subscribe();

            Category electronico = new Category("Electrónico");
            Category deporte = new Category("Deporte");
            Category muebles = new Category("Muebles");
            Category decoracion = new Category("Decoración");

            Flux.just(electronico, deporte, muebles, decoracion)
                    .flatMap(this.productService::saveCategory)
                    .doOnNext(category -> LOG.info("Categoría creada: {}", category))
                    .thenMany(
                            Flux.just(
                                            new Product("Tv LG 70'", 3609.40, electronico),
                                            new Product("Sony Cámara HD", 680.60, electronico),
                                            new Product("Bicicleta Monteñera", 1800.60, deporte),
                                            new Product("Monitor 27' LG", 750.00, electronico),
                                            new Product("Teclado Micronics", 17.00, electronico),
                                            new Product("Celular Huawey", 900.00, electronico),
                                            new Product("Interruptor simple", 6.00, decoracion),
                                            new Product("Pintura Satinado", 78.00, decoracion),
                                            new Product("Pintura Base", 10.00, decoracion),
                                            new Product("Sillón 3 piezas", 10.00, muebles),
                                            new Product("Separador para TV", 10.00, muebles),
                                            new Product("Armario 2 puertas", 910.00, muebles),
                                            new Product("Colchón Medallón 2 plazas", 710.00, muebles),
                                            new Product("Silla de oficina", 540.00, muebles)
                                    )
                                    .flatMap(product -> {
                                        product.setCreateAt(LocalDate.now());
                                        return this.productService.saveProduct(product);
                                    })

                    )
                    .subscribe(
                            product -> LOG.info("Insertado: {}, {}, {}, {}", product.getId(), product.getName(), product.getCreateAt(), product.getCategory()),
                            error -> LOG.error("Error al insertar: {}", error.getMessage()),
                            () -> LOG.info("¡Inserción completada!")
                    );
        };
    }

}
