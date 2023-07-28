package com.magadiflo.webflux.app;

import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.repositories.IProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@SpringBootApplication
public class SpringBootWebfluxApplication {

    private final static Logger LOG = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
    private final IProductRepository productRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public SpringBootWebfluxApplication(IProductRepository productRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productRepository = productRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            this.reactiveMongoTemplate.dropCollection("products").subscribe();
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
                    .flatMap(product -> {
                        product.setCreateAt(LocalDateTime.now());
                        return this.productRepository.save(product);
                    })
                    .subscribe(
                            product -> LOG.info("Insertado: {}, {}, {}", product.getId(), product.getName(), product.getCreateAt()),
                            error -> LOG.error("Error al insertar: {}", error.getMessage()),
                            () -> LOG.info("¡Inserción completada!")
                    );

        };
    }

}
