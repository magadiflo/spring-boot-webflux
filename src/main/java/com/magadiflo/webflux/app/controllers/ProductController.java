package com.magadiflo.webflux.app.controllers;

import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.repositories.IProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

@Controller
@RequestMapping(path = {"/", "/products"})
public class ProductController {
    private final IProductRepository productRepository;

    public ProductController(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping(path = {"/", "/list"})
    public String list(Model model) {
        Flux<Product> productFlux = this.productRepository.findAll(); //No es necesario subscribirnos para mostrar los datos en la vista thymeleaft.. es este quien lo hace por nosotros
        model.addAttribute("products", productFlux);
        model.addAttribute("title", "Listado de productos");
        return "list";
    }
}
