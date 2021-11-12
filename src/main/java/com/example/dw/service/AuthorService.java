package com.example.dw.service;

import com.example.dw.entity.Author;
import com.google.common.collect.ImmutableMap;

import java.util.List;

public class AuthorService extends AbstractService<Author> {

    public List<Author> findByIds(List<Long> ids) {
        return dao.find(entityClass, "Author.findByIds", ImmutableMap.of("ids", ids));
    }
}