package com.example.dw.service;

import com.example.dw.entity.Book;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookService extends AbstractService<Book> {

    public List<Book> findBookByTitle(final Optional<String> title) {
        if (title.isPresent()) {
            return dao.find(entityClass, "Book.findByTitle", ImmutableMap.of("title", title.get()));
        } else {
            return new ArrayList<>(0);
        }
    }

}