package com.example.dw;

import com.example.dw.entity.Author;
import com.example.dw.entity.Book;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.List;

public class DemoWriteMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("DefaultUnit");
        try {
            persistEntity(emf);
            nativeQueries(emf);
            loadEntityA(emf);
            loadEntityB(emf);
        } finally {
            emf.close();
        }
    }

    private static void nativeQueries(EntityManagerFactory emf) {
        System.out.println("-- native queries --");
        EntityManager em = emf.createEntityManager();
        DemoMain.nativeQuery(em, "Select * from Author");
        DemoMain.nativeQuery(em, "Select * from Book");
    }

    private static void persistEntity(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        Book book1 = new Book();
        book1.setPrice(100);
        book1.setDescription("C Programming");
        book1.setTitle("C");

        Book book2 = new Book();
        book2.setPrice(200);
        book2.setDescription("Java Programming");
        book2.setTitle("Java");

        Author author = new Author();
        author.setAge(20);
        author.setTitle("Bob");
        author.setBooks(Arrays.asList(book1, book2));

        book1.setAuth(author);
        book2.setAuth(author);

        System.out.println("-- persisting entities --");
        System.out.printf(" %s%n Author#Books: %s%n", book1, author.getBooks());
        System.out.printf(" %s%n book1#author: %s%n", book1, book1.getAuth());
        System.out.printf(" %s%n book2#author: %s%n", book2, book2.getAuth());

        em.getTransaction().begin();
        em.persist(author);
        em.persist(book1);
        em.persist(book2);
        em.getTransaction().commit();

        em.close();
    }

    private static void loadEntityA(EntityManagerFactory emf) {
        System.out.println("-- loading Author --");
        EntityManager em = emf.createEntityManager();
        List<Author> authorList = em.createQuery("Select t from Author t").getResultList();
        authorList.forEach(e -> System.out.printf(" %s%n author#books: %s%n", e, e.getBooks()));
        em.close();
    }

    private static void loadEntityB(EntityManagerFactory emf) {
        System.out.println("-- Loading Books --");
        EntityManager em = emf.createEntityManager();
        List<Book> bookList = em.createQuery("Select t from Book t").getResultList();
        bookList.forEach(e -> System.out.printf(" %s%n book#auth: %s%n", e, e.getAuth()));
        em.close();
    }
}