package com.example.dw.service;

import com.example.dw.datafetchers.BookFilter;
import com.example.dw.datafetchers.Pagination;
import com.example.dw.entity.Book;
import com.google.common.collect.ImmutableMap;

import java.util.*;

public class BookService extends AbstractService<Book> {

    public List<Book> findBookByTitle(final Optional<String> title) {
        if (title.isPresent()) {
            return dao.find(entityClass, "Book.findByTitle",
                    ImmutableMap.of("title", title.get()), 0);
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
                    return dao.find(entityClass, "Book.findByPriceLt", ImmutableMap.of("price", price ), 0);
                case "le":
                    return dao.find(entityClass, "Book.findByPriceLe", ImmutableMap.of("price", price ), 0);
                case "gt":
                    return dao.find(entityClass, "Book.findByPriceGt", ImmutableMap.of("price", price ), 0);
                case "ge":
                    return dao.find(entityClass, "Book.findByPriceGe", ImmutableMap.of("price", price ), 0);
                case "eq":
                    return dao.find(entityClass, "Book.findByPriceEq", ImmutableMap.of("price", price ), 0);
            }
        }
        if (filter.getTitle() != null) {
            System.out.println(filter.getTitle());
        }
        return null;
    }

    public Object findBookByFilter(BookFilter filter, Pagination pagination) {
        if (filter.getPrice() != null) {
            String operator = filter.getPrice().getOperator();
            int price = Integer.parseInt(filter.getPrice().getValue());
            int limit = pagination.getLimit();
            int offset = pagination.getOffset();
            switch(operator) {
                case "lt":
                    return dao.find(entityClass, "Book.findByPriceLt", ImmutableMap.of("price", price ), limit);
                case "le":
                    return dao.find(entityClass, "Book.findByPriceLe", ImmutableMap.of("price", price ), limit);
                case "gt":
                    return dao.find(entityClass, "Book.findByPriceGt", ImmutableMap.of("price", price ), limit);
                case "ge":
                    return dao.find(entityClass, "Book.findByPriceGe", ImmutableMap.of("price", price ), limit);
                case "eq":
                    return dao.find(entityClass, "Book.findByPriceEq", ImmutableMap.of("price", price ), limit);
            }
        }
        if (filter.getTitle() != null) {
            System.out.println(filter.getTitle());
        }
        return null;
    }
}