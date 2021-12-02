package com.example.dw.service;

import com.example.dw.datafetchers.BookFilter;
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

    public List<Book> findBookByFilter(BookFilter filter) {
        if (filter.getPrice() != null) {
            String operator = filter.getPrice().getOperator();
            int price = Integer.parseInt(filter.getPrice().getValue());
            switch(operator) {
                case "lt":
                    return dao.find(entityClass, "Book.findByPriceLt", ImmutableMap.of("price", price ));
                case "le":
                    return dao.find(entityClass, "Book.findByPriceLe", ImmutableMap.of("price", price ));
                case "gt":
                    return dao.find(entityClass, "Book.findByPriceGt", ImmutableMap.of("price", price ));
                case "ge":
                    return dao.find(entityClass, "Book.findByPriceGe", ImmutableMap.of("price", price ));
                case "eq":
                    return dao.find(entityClass, "Book.findByPriceEq", ImmutableMap.of("price", price ));
            }
        }
        if (filter.getTitle() != null) {
            System.out.println(filter.getTitle());
        }
        return null;
    }
}