package com.example.dw.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Table(name = "book")
@Entity
@NamedQueries({
        @NamedQuery(name = "Book.findByTitle", query = "SELECT b FROM com.example.dw.entity.Book b WHERE b.title LIKE :title"),
        @NamedQuery(name = "Book.findByPriceGt", query = "SELECT b FROM com.example.dw.entity.Book b WHERE b.price > :price"),
        @NamedQuery(name = "Book.findByPriceGe", query = "SELECT b FROM com.example.dw.entity.Book b WHERE b.price >= :price"),
        @NamedQuery(name = "Book.findByPriceLt", query = "SELECT b FROM com.example.dw.entity.Book b WHERE b.price < :price"),
        @NamedQuery(name = "Book.findByPriceLe", query = "SELECT b FROM com.example.dw.entity.Book b WHERE b.price <= :price"),
        @NamedQuery(name = "Book.findByPriceEq", query = "SELECT b FROM com.example.dw.entity.Book b WHERE b.price = :price")
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "authorId")
    private Long authorId;

    @Column(name = "description")
    private String description;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "price")
    private int price;

    @ManyToOne()
    private Author auth;
}
