package com.example.dw.resource;

import com.example.dw.entity.Book;
import com.example.dw.service.BookService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/book")
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookService bookService;

    @Inject
    public BookResource(final BookService bookService) {
        this.bookService = bookService;
    }

    @POST
    public Book addBook(@Valid Book book) {
        bookService.save(book);
        return book;
    }

    @GET
    @Path("/{id}")
    public Book getBook(@PathParam("id") Long id) {
        return bookService.findById(id);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") Long id) {
        bookService.deleteById(id);
        return Response.noContent().build();
    }

    @GET
    public List<Book> getBookList() {
        return bookService.findAll();
    }
}