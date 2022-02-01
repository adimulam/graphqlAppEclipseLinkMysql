package com.example.dw.entity;

import javax.persistence.*;
import java.util.List;

@Table(name = "author")
@Entity
@NamedQueries({
        @NamedQuery(name = "Author.findByIds", query = "SELECT a FROM com.example.dw.entity.Author a WHERE a.id IN :ids")
})
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "age")
    private Integer age;

    @Column(name = "title", nullable = false)
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "auth")
    private List<Book> books;
}
