package com.example.dw.datafetchers;

import com.example.dw.entity.Book;
import com.example.dw.entity.Author;
import com.example.dw.service.BookService;
import com.example.dw.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    /* Utilities */
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

    private static JSONObject createJSONObject(String jsonString){
        JSONObject  jsonObject=new JSONObject();
        JSONParser jsonParser=new  JSONParser();
        if ((jsonString != null) && !(jsonString.isEmpty())) {
            try {
                jsonObject=(JSONObject) jsonParser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /* PoC fetchers
    public DataFetcher getBookByIdDataFetcherOld() {
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

    public DataFetcher getBookOriginWorks() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            Long id = book.getId();
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9090/books/id/")
                    .queryParam("id", id)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            return jsonResponse.get("contentOfOrigin");
        };
    }

    public DataFetcher getBookOrigin() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            Long id = book.getId();
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9090/books/id/")
                    .queryParam("id", id)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("countryOfOrigin", jsonResponse.get("countryOfOrigin"));
            objectMap.put("publisher", jsonResponse.get("publisher"));
            return objectMap;
        };
    }

    public DataFetcher getBookPublisher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            Long id = book.getId();
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9090/books/id/")
                    .queryParam("id", id)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            return jsonResponse.get("publisher");
        };
    }

    public DataFetcher getBookContact() {
        return dataFetchingEnvironment -> {
            Map<String, Object> map = dataFetchingEnvironment.getSource();
            Long id = (Long)(map.get("id"));
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9092/books/id/")
                    .queryParam("id", id)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            return jsonResponse.get("contact");
        };
    }

    public DataFetcher getBookContactWorks() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            Long id = book.getId();
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9092/books/id/")
                    .queryParam("id", id)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            return jsonResponse.get("contact");
        };
    }

    public BatchLoader<Long, Author> authorBatchLoaderOld() {
        return authorIds ->
                CompletableFuture.supplyAsync(() -> {
                    List<Author> authors = authorIds
                            .stream()
                            .distinct()
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

    public GraphQLCodeRegistry.Builder generateBookPublisherFetcher(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(
                "Book", "countryOfOrigin"), this.getBookOrigin());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(
                "Book", "publisher"), this.getBookPublisher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(
                "Book", "contact"), this.getBookContact());
        return codeRegistryBuilder;
    }
    */

    public DataFetcher createBook() {
        return dataFetchingEnvironment -> {
            ObjectMapper objectMapper = new ObjectMapper();
            if (dataFetchingEnvironment.getArgument("filters") != null) {
                Object payload = dataFetchingEnvironment.getArgument("filters");
                Book book = objectMapper.convertValue(payload, Book.class);
                System.out.println(payload);
                return bookService.save(book);
            }
            return null;
        };
    }

    public DataFetcher getBookByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            String bookId = dataFetchingEnvironment.getArgument("id");
            Book book = bookService.findById(Long.parseLong(bookId));
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9090/books/id/")
                    .queryParam("id", bookId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("id", book.getId());
            objectMap.put("price", book.getPrice());
            objectMap.put("description", book.getDescription());
            objectMap.put("title", book.getTitle());
            objectMap.put("authorId", book.getAuthorId());
            objectMap.put("countryOfOrigin", jsonResponse.get("countryOfOrigin"));
            objectMap.put("publisher", jsonResponse.get("publisher"));
            objectMap.put("contact", jsonResponse.get("contact"));
            return objectMap;
        };
    }

    public DataFetcher getAllBooksDataFetcher() {
        return dataFetchingEnvironment -> {
            printDataFetchingEnv(dataFetchingEnvironment);
            System.out.println(bookService.findAll().getClass());
            return bookService.findAll();
        };
    }

    public DataFetcher getBooksByFilterDataFetcher() {
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
        return ids ->
                CompletableFuture.supplyAsync(() -> authorService.findByIds(ids.stream().distinct().collect(Collectors.toList())));
    }

    public GraphQLCodeRegistry.Builder generateBookFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "books"), this.getAllBooksDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "book"), this.getBookByIdDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "booksWithFilter"), this.getBooksByFilterDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "booksAggregator"), this.booksAggregator());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Mutation", "createBook"), this.createBook());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateAuthorFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "author"), this.getAuthorDataFetcherWithDataLoader());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateMutationFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Mutation", "createBook"), this.createBook());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateQueryFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = generateAuthorFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateBookFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateMutationFetchers(codeRegistryBuilder);
        return codeRegistryBuilder;
    }
}
