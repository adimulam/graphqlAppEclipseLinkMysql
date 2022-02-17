package com.example.dw.eclipselink.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.metamodel.MetamodelImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicTypeBuilder;
import org.eclipse.persistence.sessions.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.metamodel.Metamodel;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Getter
public class EntityManagerFactoryUtil {

    private EntityManager entityManager;
    private JPADynamicHelper jpaDynamicHelper;
    private Session session;
    private DynamicClassLoader dynamicClassLoader;

    public EntityManager getEntityManager() {
        return entityManager == null ? createEntityManager(): entityManager;
    }

    public EntityManager createEntityManager() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("DefaultUnit");
        return emf.createEntityManager();
    }

    public void createDynamicEntities() {
        entityManager = getEntityManager();
        session = JpaHelper.getEntityManager(entityManager).getServerSession();
        dynamicClassLoader = DynamicClassLoader.lookup(session);
        jpaDynamicHelper = new JPADynamicHelper(entityManager);
        Class<?> bookClass = dynamicClassLoader.createDynamicClass("com.example.dw.entity.NewBook");
        Class<?> authorClass = dynamicClassLoader.createDynamicClass("com.example.dw.entity.NewAuthor");

        JPADynamicTypeBuilder book = new JPADynamicTypeBuilder(bookClass, null, "Book");
        book.setPrimaryKeyFields("ID");
        book.addDirectMapping("id", int.class, "ID");
        book.addDirectMapping("description", String.class, "DESCRIPTION");
        book.addDirectMapping("title", String.class, "TITLE");
        book.addDirectMapping("price", int.class, "PRICE");
        book.addDirectMapping("authorId", String.class, "AUTHORID");

        JPADynamicTypeBuilder author = new JPADynamicTypeBuilder(authorClass, null, "Author");
        author.setPrimaryKeyFields("ID");
        author.addDirectMapping("id", int.class, "ID");
       // author.addDirectMapping("title", String.class, "TITLE");
        author.addDirectMapping("age", int.class, "AGE");
        // the name should be dynamic class Name
        author.addOneToManyMapping("com.example.dw.entity.NewBook", book.getType(), "authorId");

        jpaDynamicHelper.addTypes(false, false, book.getType());
        jpaDynamicHelper.addTypes(false, false, author.getType());

        // Refresh the metamodel
        Metamodel metamodel = (Metamodel) new MetamodelImpl((AbstractSession) session);
        ((EntityManagerFactoryImpl) JpaHelper.getEntityManagerFactory(entityManager)).setMetamodel(metamodel);
        log.debug("Refreshed the metamodel.");

        log.info("Created Dynamic Entities.");
    }

    public void createNativeQueryUtil(EntityManager em, String s) {
        log.info("Running Query: -----------------------------%n'%s'%n",  s);
        Query query = em.createNativeQuery(s);
        List list = query.getResultList();
        for (Object o : list) {
            if(o instanceof Object[]) {
                log.info(Arrays.toString((Object[]) o));
            } else {
                log.info(o.toString());
            }
        }
    }
}
