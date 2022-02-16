package com.example.dw.service;

import com.example.dw.dao.Dao;
import io.dropwizard.util.Generics;

import javax.inject.Inject;
import java.util.List;

public abstract class AbstractService<T> {

    @Inject
    protected Dao dao;

    protected final Class<T> entityClass;

    public AbstractService() {
        this.entityClass = (Class<T>) Generics.getTypeParameter(getClass());
    }

    public T save(final T object) {
        return dao.persist(object);
    }

    public T update(final T object) {
        return dao.merge(object);
    }

    public <ID> T findById(final ID id) {
        System.out.println("In findById()");
        return dao.findById(entityClass, id);
    }

    public List<T> findAll() {
        return dao.findAll(entityClass);
    }

    public <ID> ID delete(final ID id) {
        dao.removeById(entityClass, id);
        return id;
    }

    public <ID> void deleteById(final ID id) {
        dao.removeById(entityClass, id);
    }

}