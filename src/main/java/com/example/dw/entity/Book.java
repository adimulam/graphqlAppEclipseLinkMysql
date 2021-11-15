package com.example.dw.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Table(name = "book")
@Entity
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

    @ManyToOne()
    private Author auth;
}
