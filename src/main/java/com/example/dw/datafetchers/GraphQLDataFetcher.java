package com.example.dw.datafetchers;

import com.example.dw.entity.Book;
import com.example.dw.entity.Author;
import com.example.dw.service.BookService;
import com.example.dw.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import graphql.schema.DataFetcher;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.eclipse.persistence.jpa.jpql.parser.TrimExpression;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GraphQLDataFetcher {

    private final BookService bookService;
    private final AuthorService authorService;

    @Inject
    public GraphQLDataFetcher(BookService bookService, AuthorService authorService) {
        this.bookService = bookService;
        this.authorService = authorService;
    }

    private void printDataFetchingEnv(DataFetchingEnvironment dataFetchingEnvironment) {
        Map<String, Object> args = dataFetchingEnvironment.getArguments();
        for (String key: args.keySet()) {
            System.out.println(key);
            System.out.println(args.get(key));
        }
        for (Field field : dataFetchingEnvironment.getFields()) {
            System.out.println(
                    field.getSelectionSet().toString() + '\n' +
                    field.getArguments() + '\n' +
                    field.getChildren() + '\n' +
                    field.getName() + '\n'
            );
        }
    }

    public DataFetcher getBookByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            String bookId = dataFetchingEnvironment.getArgument("id");
            return bookService.findById(Long.parseLong(bookId));
        };
    }

    public DataFetcher getAuthorDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            Long authorId = book.getAuthorId();
            return authorService.findById(authorId);
        };
    }

    public DataFetcher getAllBooksDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            return bookService.findAll();
        };
    }

    public DataFetcher getBooksByFilterDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            ObjectMapper objectMapper = new ObjectMapper();
            if (dataFetchingEnvironment.getArgument("filters") != null
                && dataFetchingEnvironment.getArgument("pagination") == null) {
                Object filterInput = dataFetchingEnvironment.getArgument("filters");
                BookFilter filterObject = objectMapper.convertValue(filterInput, BookFilter.class);
                System.out.println(filterObject);
                return bookService.findBookByFilter(filterObject);
            }

            if (dataFetchingEnvironment.getArgument("filters") != null
                && dataFetchingEnvironment.getArgument("pagination") != null) {
                Object filterInput = dataFetchingEnvironment.getArgument("filters");
                Object paginationInput = dataFetchingEnvironment.getArgument("pagination");
                BookFilter filterObject = objectMapper.convertValue(filterInput, BookFilter.class);
                Pagination paginationObject = objectMapper.convertValue(paginationInput, Pagination.class);
                System.out.println(filterObject);
                System.out.println(paginationObject);
                return bookService.findBookByFilter(filterObject,paginationObject);
            }

            return null;
        };
    }


    public DataFetcher getBooksByFilterDataFetcherNew() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            BookFilter filterObject = null;
            Pagination paginationObject = null;
            Boolean distinct = null;
            SortByInput sort = null;

            ObjectMapper objectMapper = new ObjectMapper();
            Object filters = dataFetchingEnvironment.getArgument("filters");
            Object pagination = dataFetchingEnvironment.getArgument("pagination");
            Object distinctOn = dataFetchingEnvironment.getArgument("distinctOn");
            Object sortBy = dataFetchingEnvironment.getArgument("sort");
            if (filters != null) {
                filterObject = objectMapper.convertValue(filters, BookFilter.class);
            }
            if (pagination != null) {
                paginationObject = objectMapper.convertValue(pagination, Pagination.class);
            }
            if (distinctOn != null) {
                distinct = objectMapper.convertValue(distinctOn, Boolean.class);
            }
            if (sortBy != null) {
                sort = objectMapper.convertValue(sortBy, SortByInput.class);
            }
            return bookService.findBookByFilter(filterObject, paginationObject, distinct, sort);
        };
    }

    public DataFetcher getBooksByFilterDataFetcherFinal() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            Object filters = dataFetchingEnvironment.getArgument("filters");
            Object pagination = dataFetchingEnvironment.getArgument("pagination");
            Object distinctOn = dataFetchingEnvironment.getArgument("distinctOn");
            Object sortBy = dataFetchingEnvironment.getArgument("sort");
            return bookService.findBookByFilterFinal(filters, pagination, distinctOn, sortBy);
        };
    }


    private DataFetcher<?> booksAggregator() {
        return dataFetchingEnvironment -> {
            Object aggregation = dataFetchingEnvironment.getArgument("aggregation");
            return bookService.findAggregation(aggregation);
        };
    }

    public DataFetcher getAuthorDataFetcherWithDataLoader() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            Book book = dataFetchingEnvironment.getSource();
            Long authorId = book.getAuthorId();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> authorLoader = dataLoaderRegistry.getDataLoader("authors");
            return authorLoader.load(authorId);
        };
    }

    public BatchLoader<Long, Author> authorBatchLoader() {
        return ids -> CompletableFuture.supplyAsync(() -> authorService.findByIds(ids));
    }

    public BatchLoader<Long, Author> authorBatchLoaderOld() {
        return authorIds ->
                CompletableFuture.supplyAsync(() -> {
                    List<Author> authors = authorIds
                            .stream()
                            .map(id -> authorService.findById(id))
                            .collect(Collectors.toList());
                    return authors;
                });
    }

    public DataFetcher getBookByNameDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            String bookName= dataFetchingEnvironment.getArgument("name");
            return bookService.findBookByTitle(Optional.of(bookName));
        };
    }

    public DataFetcher getAllAuthorsDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            return authorService.findAll();
        };
    }

    public GraphQLCodeRegistry.Builder generateBookFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "books"), this.getAllBooksDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "book"), this.getBookByIdDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "booksWithFilter"), this.getBooksByFilterDataFetcherFinal());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "booksAggregator"), this.booksAggregator());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateAuthorFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "author"), this.getAuthorDataFetcherWithDataLoader());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "authors"), this.getAllAuthorsDataFetcher());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateQueryFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = generateAuthorFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateBookFetchers(codeRegistryBuilder);
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateMutationFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        return codeRegistryBuilder;
    }

    public static class GraphQLSchemaType {

        private String type;
        private List<String> fieldList = new ArrayList<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getFieldList() {
            return fieldList;
        }

        public void setFieldList(List<String> fieldList) {
            this.fieldList = fieldList;
        }
    }
}
