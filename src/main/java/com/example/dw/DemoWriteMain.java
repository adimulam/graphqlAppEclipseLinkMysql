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
        book1.setAuthorId(1L);

        Book book2 = new Book();
        book2.setPrice(200);
        book2.setDescription("Java Programming");
        book2.setTitle("Java");
        book2.setAuthorId(1L);

        Book book3 = new Book();
        book3.setPrice(300);
        book3.setDescription("JavaScript Programming");
        book3.setTitle("JavaScript");
        book3.setAuthorId(2L);

        Book book4 = new Book();
        book4.setPrice(400);
        book4.setDescription("OCI Cloud Developer Certification");
        book4.setTitle("OCI Cloud Developer");
        book4.setAuthorId(2L);

        Book book5 = new Book();
        book5.setPrice(500);
        book5.setDescription("OCI Cloud Associate Certification");
        book5.setTitle("OCI Cloud Associate");
        book5.setAuthorId(3L);

        Book book6 = new Book();
        book6.setPrice(600);
        book6.setDescription("OCI Cloud Professional Certification");
        book6.setTitle("OCI Cloud Professional");
        book6.setAuthorId(3L);

        Author author1 = new Author();
        author1.setAge(30);
        author1.setTitle("Bob");
        author1.setBooks(Arrays.asList(book1, book2));

        Author author2 = new Author();
        author2.setAge(40);
        author2.setTitle("Sam");
        author2.setBooks(Arrays.asList(book3, book4));

        Author author3 = new Author();
        author3.setAge(50);
        author3.setTitle("Smith");
        author3.setBooks(Arrays.asList(book5, book6));

        book1.setAuth(author1);
        book2.setAuth(author1);
        book3.setAuth(author2);
        book4.setAuth(author2);
        book5.setAuth(author3);
        book6.setAuth(author3);

        System.out.println("-- persisting entities --");
        System.out.printf(" %s%n Author#Books: %s%n", book1, author1.getBooks());
        System.out.printf(" %s%n book1#author: %s%n", book1, book1.getAuth());
        System.out.printf(" %s%n book2#author: %s%n", book2, book2.getAuth());

        em.getTransaction().begin();
        em.persist(author1);
        em.persist(author2);
        em.persist(author3);
        em.persist(book1);
        em.persist(book2);
        em.persist(book3);
        em.persist(book4);
        em.persist(book5);
        em.persist(book6);
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