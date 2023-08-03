package com.magadiflo.webflux.app.models.repositories;

import com.magadiflo.webflux.app.models.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IProductRepository extends ReactiveMongoRepository<Product, String> {
}
