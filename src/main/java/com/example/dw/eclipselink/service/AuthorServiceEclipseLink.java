package com.example.dw.eclipselink.service;

import com.example.dw.eclipselink.utils.EntityManagerFactoryUtil;
import com.example.dw.entity.Author;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.dynamic.DynamicEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AuthorServiceEclipseLink {

    private EntityManagerFactoryUtil entityManagerFactoryUtil;
    private EntityManager entityManager;

    @Inject
    AuthorServiceEclipseLink(EntityManagerFactoryUtil entityManagerFactoryUtil) {
        this.entityManagerFactoryUtil = entityManagerFactoryUtil;
        this.entityManager = entityManagerFactoryUtil.getEntityManager();
    }

    public Object save(Author author) {
        entityManager.persist(author);
        return author;
    }

    public Object update(Author author) {
        entityManager.merge(author);
        return entityManager.find(Author.class, author);
    }

    public Object delete(Long id) {
        entityManager.remove(entityManager.find(Author.class, id));
        return null;
    }

    public Author findByIdOld(long id) {
        return entityManager.find(Author.class, id);
    }

    public List<Author> findAllOld() {
        List<Author> authorList = entityManager.createQuery("Select a from Author a").getResultList();
        return authorList;
    }

    public List<Author> findByIds(List<Long> ids) {
        Query query = entityManager.createQuery("SELECT a FROM Author a WHERE a.id IN :keyword");
        query.setParameter("keyword", ids);
        return query.getResultList();
    }

    public Map<String, Object> findById(long bookId) {
        log.info("Finding One Author");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class modelEntity = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewBook").getJavaClass();
        CriteriaQuery<? extends DynamicEntity> cq = cb.createQuery(modelEntity);
        Root model = cq.from(modelEntity);
        cq.where(cb.equal(model.get("id"), bookId));
        Query query =  entityManager.createQuery(cq);
        DynamicEntity dynamicEntity = (DynamicEntity) query.getSingleResult();
        return prepareResponse(dynamicEntity);
    }

    public List findAll() {
        log.info("Finding All Authors");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<? extends DynamicEntity> cq = cb.createQuery(entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewAuthor").getJavaClass());
        Query query =  entityManager.createQuery(cq);
        List<DynamicEntity> dynamicEntities = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DynamicEntity dynamicEntity : dynamicEntities) {
            resultList.add(prepareResponse(dynamicEntity));
        }
        return resultList;
    }

    private Map<String, Object> prepareResponse(DynamicEntity dynamicEntity) {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("id", dynamicEntity.get("id"));
        entityMap.put("title", dynamicEntity.get("title"));
        entityMap.put("age", dynamicEntity.get("age"));
        return entityMap;
    }
}
