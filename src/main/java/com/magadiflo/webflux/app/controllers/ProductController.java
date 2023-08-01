package com.magadiflo.webflux.app.controllers;

import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.repositories.IProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
@RequestMapping(path = {"/", "/products"})
public class ProductController {
    private final static Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final IProductRepository productRepository;

    public ProductController(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

    @GetMapping(path = "/list-data-driver")
    public String listDataDriver(Model model) {
        Flux<Product> productFlux = this.productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .delayElements(Duration.ofSeconds(1));

        productFlux.subscribe(product -> LOG.info(product.getName()));

        model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 2));
        model.addAttribute("title", "Listado de productos");
        return "list";
    }

    @GetMapping(path = "/list-full")
    public String listFull(Model model) {
        Flux<Product> productFlux = this.productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .repeat(5000); //<-- Repetimos 5000 veces el flujo actual

        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list";
    }
}
