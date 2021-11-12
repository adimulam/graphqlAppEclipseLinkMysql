package com.example.dw.resource;

import com.example.dw.entity.Author;
import com.example.dw.service.AuthorService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/author")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorResource {

    private final AuthorService authorService;

    @Inject
    public AuthorResource(final AuthorService authorService) {
        this.authorService = authorService;
    }

    @POST
    public Author addAuthor(@Valid Author author) {
        authorService.save(author);
        return author;
    }

    @GET
    @Path("/{id}")
    public Author getAuthor(@PathParam("id") Long id) {
        return authorService.findById(id);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAuthor(@PathParam("id") Long id) {
        authorService.deleteById(id);
        return Response.noContent().build();
    }

    @GET
    public List<Author> getAuthorList() {
        return authorService.findAll();
    }
}