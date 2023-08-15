package com.magadiflo.webflux.app.controllers;

import com.magadiflo.webflux.app.models.documents.Category;
import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.services.IProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@SessionAttributes(value = "product")
@Controller
@RequestMapping(path = {"/", "/products"})
public class ProductController {
    private final static Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private final IProductService productService;
    @Value("${config.uploads.path}")
    private String uploadsPath;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @ModelAttribute(name = "categories")
    public Flux<Category> categories() {
        return this.productService.findAllCategories();
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
        model.addAttribute("btnText", "Crear");
        return Mono.just("form");
    }

    @PostMapping(path = "/form")
    public Mono<String> save(@Valid Product product, BindingResult result, SessionStatus sessionStatus,
                             Model model, @RequestPart FilePart imageFile) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Errores en el formulario de producto");
            model.addAttribute("btnText", "Guardar");
            return Mono.just("form");
        }
        sessionStatus.setComplete();

        return this.productService.findCategory(product.getCategory().getId())
                .flatMap(categoryDB -> {
                    if (product.getCreateAt() == null) {
                        product.setCreateAt(LocalDate.now());
                    }
                    if (!imageFile.filename().isBlank()) {
                        String filename = UUID.randomUUID().toString() + "-" + imageFile.filename()
                                .replace(" ", "")
                                // Este es un hack para que funcione en internet explorer 10 y microsoft edge
                                .replace(":", "")
                                .replace("\\", "");
                        product.setImage(filename);
                    }
                    product.setCategory(categoryDB);
                    return this.productService.saveProduct(product);
                }).doOnNext(p -> LOG.info("Producto guardado: {}", p))
                .flatMap(productDB -> {
                    if (!imageFile.filename().isBlank()) {
                        return imageFile.transferTo(new File(this.uploadsPath + productDB.getImage()));
                    }
                    return Mono.empty();
                })
                .thenReturn("redirect:/list?success=Producto+guardado+con+éxito");
    }

    @GetMapping(path = "/form/{id}")
    public Mono<String> edit(@PathVariable String id, Model model) {
        Mono<Product> productMono = this.productService.findById(id)
                .doOnNext(product -> LOG.info(product.toString()))
                .defaultIfEmpty(new Product());
        model.addAttribute("product", productMono);
        model.addAttribute("title", "Editar producto");
        model.addAttribute("btnText", "Editar");
        return Mono.just("form");
    }

    @GetMapping(path = "/form-v2/{id}")
    public Mono<String> editV2(@PathVariable String id, Model model) {
        return this.productService.findById(id)
                .doOnNext(product -> {
                    LOG.info(product.toString());
                    model.addAttribute("product", product);
                    model.addAttribute("title", "Editar producto");
                    model.addAttribute("btnText", "Editar v2");
                })
                .defaultIfEmpty(new Product())
                .flatMap(product -> {
                    if (product.getId() == null) {
                        return Mono.error(() -> new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(product);
                })
                .thenReturn("form")
                .onErrorResume(throwable -> Mono.just("redirect:/list?error=no+existe+el+producto"));
    }

    @GetMapping(path = "/delete/{id}")
    public Mono<String> delete(@PathVariable String id) {
        return this.productService.findById(id)
                .defaultIfEmpty(new Product())
                .flatMap(product -> {
                    if (product.getId() == null) {
                        return Mono.error(() -> new InterruptedException("No existe el producto a eliminar"));
                    }
                    LOG.info("Producto a eliminar: {}", product);
                    return Mono.just(product);
                })
                .flatMap(this.productService::delete)
                .then(Mono.just("redirect:/list?success=Producto+eliminado+con+éxito"))
                .onErrorResume(throwable -> Mono.just("redirect:/list?error=no+existe+el+producto"));
    }

    @GetMapping(path = "/details/{id}")
    public Mono<String> details(@PathVariable String id, Model model) {
        return this.productService.findById(id)
                .doOnNext(productDB -> {
                    LOG.info("Producto: {}", productDB);
                    model.addAttribute("product", productDB);
                    model.addAttribute("title", "Detalles del producto");
                })
                .switchIfEmpty(Mono.just(new Product())) // Es lo mismo que el defaultIfEmpty, solo que con el swithIfEmpty aquí pasamos un mono
                .flatMap(productDB -> {
                    if (productDB.getId() == null) {
                        return Mono.error(() -> new InterruptedException("No existe el producto"));
                    }
                    return Mono.empty();
                })
                .then(Mono.just("details"))// es lo mismo que el thenReturn, solo que con el then pasamos un mono
                .onErrorResume(throwable -> Mono.just("redirect:/list?error=no+existe+el+producto+para+ver+sus+detalles"));
    }

    @GetMapping(path = "/uploads/image/{imageName:.+}")
    public Mono<ResponseEntity<Resource>> showImage(@PathVariable String imageName) throws MalformedURLException {
        Path absolutePath = Paths.get(this.uploadsPath).resolve(imageName).toAbsolutePath();
        URI uri = absolutePath.toUri();

        LOG.info("absolutePath: {}", absolutePath);
        LOG.info("uri: {}", uri);

        Resource resource = new UrlResource(uri);
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + resource.getFilename() + "\"")
                .body(resource));
    }
}
