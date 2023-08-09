package com.magadiflo.webflux.app.controllers;

import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.services.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SessionAttributes(value = "product")
@Controller
@RequestMapping(path = {"/", "/products"})
public class ProductController {
    private final static Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping(path = {"/", "/list"})
    public String list(Model model) {
        Flux<Product> productFlux = this.productService.findAllWithNameUpperCase();
        productFlux.subscribe(product -> LOG.info(product.getName())); // (1)

        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list";
    }

    @GetMapping(path = "/list-data-driver")
    public String listDataDriver(Model model) {
        Flux<Product> productFlux = this.productService
                .findAllWithNameUpperCase()
                .delayElements(Duration.ofSeconds(1));

        productFlux.subscribe(product -> LOG.info(product.getName()));

        model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 2));
        model.addAttribute("title", "Listado de productos");
        return "list";
    }

    @GetMapping(path = "/list-full")
    public String listFull(Model model) {
        Flux<Product> productFlux = this.productService.findAllWithNameUpperCaseAndRepeat();

        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list";
    }

    @GetMapping(path = "/list-chunked")
    public String listChunked(Model model) {
        Flux<Product> productFlux = this.productService.findAllWithNameUpperCaseAndRepeat();

        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list-chunked";
    }

    @GetMapping(path = "/form")
    public Mono<String> create(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("title", "Formulario de producto");
        return Mono.just("form");
    }

    @PostMapping(path = "/form")
    public Mono<String> save(Product product, SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return this.productService.saveProduct(product)
                .doOnNext(p -> LOG.info("Producto guardado: {}", p))
                .thenReturn("redirect:/list");
    }

    @GetMapping(path = "/form/{id}")
    public Mono<String> edit(@PathVariable String id, Model model) {
        Mono<Product> productMono = this.productService.findById(id)
                .doOnNext(product -> LOG.info(product.toString()))
                .defaultIfEmpty(new Product());
        model.addAttribute("product", productMono);
        model.addAttribute("title", "Editar producto");
        return Mono.just("form");
    }
}
