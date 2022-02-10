package com.example.dw;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

public class DemoMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("DefaultUnit");
        try {
            EntityManager em = emf.createEntityManager();
            nativeQuery(em, "SHOW TABLES");
            nativeQuery(em, "SHOW COLUMNS from Author");
            nativeQuery(em, "SHOW COLUMNS from Book");
        } finally {
            emf.close();
        }
    }

    public static void nativeQuery(EntityManager em, String s) {
        System.out.printf("-----------------------------%n'%s'%n",  s);
        Query query = em.createNativeQuery(s);
        List list = query.getResultList();
        for (Object o : list) {
            if(o instanceof Object[]) {
                System.out.println(Arrays.toString((Object[]) o));
            } else {
                System.out.println(o);
            }
        }
    }
}