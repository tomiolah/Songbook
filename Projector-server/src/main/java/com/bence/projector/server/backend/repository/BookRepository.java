package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {
}
