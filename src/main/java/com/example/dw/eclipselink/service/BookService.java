package com.example.dw.eclipselink.service;

import com.example.dw.eclipselink.utils.EntityManagerFactoryUtil;
import com.example.dw.entity.Book;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import java.util.List;

@Slf4j
public class BookService {

    private EntityManagerFactoryUtil entityManagerFactoryUtil;

    @Inject
    BookService(EntityManagerFactoryUtil entityManagerFactoryUtil) {
        this.entityManagerFactoryUtil = entityManagerFactoryUtil;
    }

    public Book save(Book book) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(book);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return book;
    }

    public Book update(Book book) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(book);
        entityManager.flush();
        entityManager.getTransaction().commit();
        //return entityManager.find(Book.class, book);
        return book;
    }

    public Book delete(Long id) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.find(Book.class, id));
        entityManager.flush();
        entityManager.getTransaction().commit();
        return null;
    }

    public Book findById(long id) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        return entityManager.find(Book.class, id);
    }

    public List<Book> findAll() {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        List<Book> bookList = entityManager.createQuery("Select b from Book b").getResultList();
        //bookList.forEach(b -> log.info(" {}", b.toString()));
        return bookList;
    }
}
