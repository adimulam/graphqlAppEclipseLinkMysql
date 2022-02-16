package com.example.dw.eclipselink.service;

import com.example.dw.eclipselink.utils.EntityManagerFactoryUtil;
import com.example.dw.entity.Author;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Slf4j
public class AuthorService {

    private EntityManagerFactoryUtil entityManagerFactoryUtil;

    @Inject
    AuthorService(EntityManagerFactoryUtil entityManagerFactoryUtil) {
        this.entityManagerFactoryUtil = entityManagerFactoryUtil;
    }

    public Object save(Author author) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        entityManager.persist(author);
        return author;
    }

    public Object update(Author author) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        entityManager.merge(author);
        return entityManager.find(Author.class, author);
    }

    public Object delete(Long id) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        entityManager.remove(entityManager.find(Author.class, id));
        return null;
    }

    public Author findById(long id) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        return entityManager.find(Author.class, id);
    }

    public List<Author> findAll() {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        List<Author> authorList = entityManager.createQuery("Select a from Author a").getResultList();
        //authorList.forEach(a -> log.info(" {}", a.toString()));
        return authorList;
    }

    public List<Author> findByIds(List<Long> ids) {
        EntityManager entityManager = entityManagerFactoryUtil.getEntityManager();
        Query query = entityManager.createQuery("SELECT a FROM Author a WHERE a.id IN :keyword");
        query.setParameter("keyword", ids);
        return query.getResultList();
    }
}
