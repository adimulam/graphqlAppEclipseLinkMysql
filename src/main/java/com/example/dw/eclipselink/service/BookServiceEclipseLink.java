package com.example.dw.eclipselink.service;

import com.example.dw.eclipselink.utils.EntityManagerFactoryUtil;
import com.example.dw.entity.Book;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.dynamic.DynamicEntity;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BookServiceEclipseLink {

    private EntityManagerFactoryUtil entityManagerFactoryUtil;
    private EntityManager entityManager;

    @Inject
    BookServiceEclipseLink(EntityManagerFactoryUtil entityManagerFactoryUtil) {
        this.entityManagerFactoryUtil = entityManagerFactoryUtil;
        this.entityManager = entityManagerFactoryUtil.getEntityManager();
        this.entityManagerFactoryUtil.createDynamicEntities();
    }

    public Book save(Book book) {
        entityManager.getTransaction().begin();
        entityManager.persist(book);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return book;
    }

    public Book update(Book book) {
        entityManager.getTransaction().begin();
        entityManager.merge(book);
        entityManager.flush();
        entityManager.getTransaction().commit();
        //return entityManager.find(Book.class, book);
        return book;
    }

    public Book delete(Long id) {
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.find(Book.class, id));
        entityManager.flush();
        entityManager.getTransaction().commit();
        return null;
    }

    public Book findByIdOld(long id) {
        return entityManager.find(Book.class, id);
    }

    public List<Book> findAllOld() {
        List<Book> bookList = entityManager.createQuery("Select b from Book b").getResultList();
        return bookList;
    }

    public List<Object> findAllWithJoinsOld() {
        List<Object> resultList = entityManager.createQuery("SELECT b FROM Book b, Author a WHERE b.authorId = a.id").getResultList();
        resultList.forEach(b -> log.info(" {}", b.toString()));
        return resultList;
    }

    public Map<String, Object> findById(long bookId) {
        log.info("Finding One Book");
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
        log.info("Finding All Books");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<? extends DynamicEntity> cq = cb.createQuery(entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewBook").getJavaClass());
        Query query =  entityManager.createQuery(cq);
        List<DynamicEntity> dynamicEntities = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DynamicEntity dynamicEntity : dynamicEntities) {
            resultList.add(prepareResponse(dynamicEntity));
        }
        return resultList;
    }

    public List findAllRecords() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class bookClass = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewBook").getJavaClass();
        CriteriaQuery cq = cb.createQuery(bookClass);
        Root book = cq.from(bookClass);
        cq.select(book);
        TypedQuery query = entityManager.createQuery(cq);
        List<DynamicEntity> dynamicEntities = query.getResultList();
        List<Map<String, Object>> allBooks = new ArrayList<>();
        for (DynamicEntity dynamicEntity : dynamicEntities) {
            allBooks.add(prepareResponse(dynamicEntity));
        }
        return allBooks;
    }

    private Map<String, Object> prepareResponse(DynamicEntity dynamicEntity) {
        Map<String, Object> entityMap = new HashMap<>();
        entityMap.put("id", dynamicEntity.get("id"));
        entityMap.put("title", dynamicEntity.get("title"));
        entityMap.put("description", dynamicEntity.get("description"));
        entityMap.put("price", dynamicEntity.get("price"));
        entityMap.put("authorId", dynamicEntity.get("authorId"));
        return entityMap;
    }

    public List findAllWithJoins() {
        log.info("Finding All Books With Author Joined");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class modelEntity1 = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewBook").getJavaClass();
        Class modelEntity2 = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewAuthor").getJavaClass();
        CriteriaQuery cq = cb.createQuery(modelEntity1);
        Metamodel m = entityManager.getMetamodel();
        EntityType petMetaModel = m.entity(modelEntity1);
        Root book = cq.from(modelEntity1);
        Join author = book.join(petMetaModel.getSet("authorId", modelEntity2));
        log.info(author.toString());
        return null;
    }

    public List findAllEntries() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class modelEntity1 = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewBook").getJavaClass();
        Class modelEntity2 = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewAuthor").getJavaClass();
        List<Class> entityClass = new ArrayList<>();
        entityClass.add(modelEntity1);
        entityClass.add(modelEntity2);

        CriteriaQuery cq = cb.createQuery(entityClass.get(1));
        Root root = cq.from(entityClass.get(1));

        //For fetch to work there should be mapping added . Hence cannot query author from book . Write custom Many-to-one mapping
        root.fetch(entityClass.get(0).getName(), JoinType.LEFT);

        TypedQuery q = entityManager.createQuery(cq);
        List resultList = q.getResultList();
        List<DynamicEntity> res = (List<DynamicEntity>) resultList;
        for (DynamicEntity entity : res) {
            log.info(entity.get("id").toString());
            log.info(entity.get("title"));
            log.info(entity.get("age"));
            DynamicEntity lap = entity.get("book");
            if( null != lap) {
                log.info(lap.get("id"));
                log.info(lap.get("title").toString());
                log.info(lap.get("price").toString());
            }
        }
        return null;
    }

    public List findAllWithRelations() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        Class modelEntity1 = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewBook").getJavaClass();
        Class modelEntity2 = entityManagerFactoryUtil.getJpaDynamicHelper().getType("com.example.dw.entity.NewAuthor").getJavaClass();
        List<Class> entityClass = new ArrayList<>();
        entityClass.add(modelEntity1);
        entityClass.add(modelEntity2);

        CriteriaQuery cq = cb.createQuery(entityClass.get(0));
        Root book = cq.from(entityClass.get(0));
        //Join author = book.join(Book_.author);

        return null;
    }

}
