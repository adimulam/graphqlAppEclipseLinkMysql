package com.example.dw.eclipselink.utils;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EntityManagerFactoryUtil {

    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager == null ? createEntityManager(): entityManager;
    }

    public EntityManager createEntityManager() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("DefaultUnit");
        return emf.createEntityManager();
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
