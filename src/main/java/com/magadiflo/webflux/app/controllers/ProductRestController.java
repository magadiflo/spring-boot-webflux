package com.magadiflo.webflux.app.controllers;

import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.repositories.IProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductRestController {
    private final static Logger LOG = LoggerFactory.getLogger(ProductRestController.class);
    private final IProductRepository productRepository;

    public ProductRestController(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public Flux<Product> listProducts() {
        return this.productRepository.findAll()
                .doOnNext(product -> LOG.info(product.getName())); //<-- doOnNext(), dispara efecto secundario, similar al tap() del RxJs. Es importante tener en cuenta que doOnNext() no modifica el flujo de datos original.
    }

    @GetMapping(path = "/{id}")
    public Mono<Product> showProduct(@PathVariable String id) {
        return this.productRepository.findById(id)
                .doOnNext(product -> LOG.info(product.getName()));
    }

}
