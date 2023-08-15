package com.magadiflo.webflux.app.models.repositories;

import com.magadiflo.webflux.app.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ICategoryRepository extends ReactiveMongoRepository<Category, String> {
}
