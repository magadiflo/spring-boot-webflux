package com.magadiflo.webflux.app.models.services.impl;

import com.magadiflo.webflux.app.models.documents.Category;
import com.magadiflo.webflux.app.models.documents.Product;
import com.magadiflo.webflux.app.models.repositories.ICategoryRepository;
import com.magadiflo.webflux.app.models.repositories.IProductRepository;
import com.magadiflo.webflux.app.models.services.IProductService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements IProductService {
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;

    public ProductServiceImpl(IProductRepository productRepository, ICategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Flux<Product> findAll() {
        return this.productRepository.findAll();
    }

    @Override
    public Flux<Product> findAllWithNameUpperCase() {
        return this.productRepository.findAll()
                .map(product -> {
                    product.setName(product.getName().toUpperCase());
                    return product;
                });
    }

    @Override
    public Flux<Product> findAllWithNameUpperCaseAndRepeat() {
        return this.findAllWithNameUpperCase().repeat(5000);
    }

    @Override
    public Mono<Product> findById(String id) {
        return this.productRepository.findById(id);
    }

    @Override
    public Mono<Product> saveProduct(Product product) {
        return this.productRepository.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return this.productRepository.delete(product);
    }

    @Override
    public Flux<Category> findAllCategories() {
        return this.categoryRepository.findAll();
    }

    @Override
    public Mono<Category> findCategory(String id) {
        return this.categoryRepository.findById(id);
    }

    @Override
    public Mono<Category> saveCategory(Category category) {
        return this.categoryRepository.save(category);
    }
}
