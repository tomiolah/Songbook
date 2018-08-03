package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.repository.BookRepository;
import com.bence.projector.server.backend.service.BibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BibleServiceImpl extends BaseServiceImpl<Bible> implements BibleService {
    private final BookRepository bookRepository;

    @Autowired
    public BibleServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Bible save(Bible model) {
        bookRepository.save(model.getBooks());
        return super.save(model);
    }

    @Override
    public Iterable save(List<Bible> models) {
        for (Bible bible : models) {
            save(bible);
        }
        return models;
    }

    @Override
    public void delete(String id) {
        Bible bible = findOne(id);
        bookRepository.delete(bible.getBooks());
        super.delete(id);
    }

    @Override
    public void delete(List<String> ids) {
        for (String id : ids) {
            delete(id);
        }
    }
}
