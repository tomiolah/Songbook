package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl extends BaseServiceImpl<Book> implements BookService {
    @Override
    public Book findOneByUuid(String uuid) {
        return null;
    }

    @Override
    public void deleteByUuid(String uuid) {

    }
}
