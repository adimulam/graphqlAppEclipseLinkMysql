package com.example.dw.datafetchers;

import com.example.dw.entity.Book;
import com.example.dw.entity.Author;
import com.example.dw.eclipselink.service.BookServiceEclipseLink;
import com.example.dw.eclipselink.service.AuthorServiceEclipseLink;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.*;
import graphql.language.Field;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@Slf4j
public class GraphQLDataFetcher {

    private final BookServiceEclipseLink bookServiceEclipseLink;
    private final AuthorServiceEclipseLink authorServiceEclipseLink;

    @Inject
    public GraphQLDataFetcher(BookServiceEclipseLink bookServiceEclipseLink, AuthorServiceEclipseLink authorServiceEclipseLink) {
        this.bookServiceEclipseLink = bookServiceEclipseLink;
        this.authorServiceEclipseLink = authorServiceEclipseLink;
    }

    /* Utilities */
    private void printDataFetchingEnv(DataFetchingEnvironment dataFetchingEnvironment) {
        Map<String, Object> args = dataFetchingEnvironment.getArguments();
        List<SelectedField> fields = dataFetchingEnvironment.getSelectionSet().getFields();
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
                return bookServiceEclipseLink.save(book);
            }
            return null;
        };
    }

    public DataFetcher updateBook() {
        return dataFetchingEnvironment -> {
            ObjectMapper objectMapper = new ObjectMapper();
            if (dataFetchingEnvironment.getArgument("filters") != null) {
                Object payload = dataFetchingEnvironment.getArgument("filters");
                Book book = objectMapper.convertValue(payload, Book.class);
                System.out.println(payload);
                return bookServiceEclipseLink.update(book);
            }
            return null;
        };
    }

    public DataFetcher deleteBook() {
        return dataFetchingEnvironment -> {
            ObjectMapper objectMapper = new ObjectMapper();
            if (dataFetchingEnvironment.getArgument("filters") != null) {
                Object payload = dataFetchingEnvironment.getArgument("filters");
                Book book = objectMapper.convertValue(payload, Book.class);
                System.out.println(payload);
                return bookServiceEclipseLink.delete(book.getId());
            }
            return null;
        };
    }

    private Map<String, Object> prepareResponsePayload(Map<String, Object> book, Response response) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("id", book.get("id"));
        objectMap.put("price", book.get("price"));
        objectMap.put("description", book.get("description"));
        objectMap.put("title", book.get("title"));
        objectMap.put("authorId", book.get("authorId"));
        if (response != null) {
            JSONObject jsonResponse = createJSONObject(response.readEntity(String.class));
            objectMap.put("countryOfOrigin", jsonResponse.get("countryOfOrigin"));
            objectMap.put("publisher", jsonResponse.get("publisher"));
            objectMap.put("contact", jsonResponse.get("contact"));
            objectMap.put("numOfPages", jsonResponse.get("pages"));
        }
        return objectMap;
    }

    public DataFetcher getHeroDataFetcher() {
        return  dataFetchingEnvironment -> {
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9080/heroes/")
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
            return resultObjs;
        };
    }

    public DataFetcher getBookByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            //printDataFetchingEnv(dataFetchingEnvironment);
            String bookId = dataFetchingEnvironment.getArgument("id");
            Map<String, Object> book = bookServiceEclipseLink.findById(Long.parseLong(bookId)); //ds1 - mysqlDB

            // REST
            Client client = ClientBuilder.newClient();
            Response response = client.target("http://localhost:9090/books/id/")
                    .queryParam("id", bookId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            return prepareResponsePayload(book, response);
        };
    }

    private List<String> getPages(List<Long> bookIds) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9090/books/ids/")
                .queryParam("ids", StringUtils.join(bookIds, ','))
                .request(MediaType.APPLICATION_JSON)
                .get();
        List<String> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
        for (Object o : resultObjs) {
            String jsonResponse = mapper.writeValueAsString(o);
            JSONObject resultJson = createJSONObject(jsonResponse);
            results.add(resultJson.get("pages").toString());
        }
        return results;
    }

    private List<String> getContact(List<Long> bookIds) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9090/books/ids/")
                .queryParam("ids", StringUtils.join(bookIds, ','))
                .request(MediaType.APPLICATION_JSON)
                .get();
        List<String> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
        for (Object o : resultObjs) {
            String jsonResponse = mapper.writeValueAsString(o);
            JSONObject resultJson = createJSONObject(jsonResponse);
            results.add(resultJson.get("contact").toString());
        }
        return results;
    }

    private List<String> getPublisher(List<Long> bookIds) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9090/books/ids/")
                .queryParam("ids", StringUtils.join(bookIds, ','))
                .request(MediaType.APPLICATION_JSON)
                .get();
        List<String> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
        for (Object o : resultObjs) {
            String jsonResponse = mapper.writeValueAsString(o);
            JSONObject resultJson = createJSONObject(jsonResponse);
            results.add(resultJson.get("publisher").toString());
        }
        return results;
    }

    private List<String> getCountryOfOrigin(List<Long> bookIds) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9090/books/ids/")
                .queryParam("ids", StringUtils.join(bookIds, ','))
                .request(MediaType.APPLICATION_JSON)
                .get();
        List<String> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
        for (Object o : resultObjs) {
            Map<String, String> objectMap = new HashMap<>();
            String jsonResponse = mapper.writeValueAsString(o);
            JSONObject resultJson = createJSONObject(jsonResponse);
            //System.out.println(resultJson.get("countryOfOrigin"));
            //objectMap.put("countryOfOrigin", resultJson.get("countryOfOrigin").toString());
            //objectMap.put("publisher", resultJson.get("publisher"));
            //objectMap.put("contact", resultJson.get("contact"));
            //objectMap.put("numOfPages", resultJson.get("pages"));
            results.add(resultJson.get("countryOfOrigin").toString());
            //results.add(resultJson.get("publisher").toString());
            //results.add(resultJson.get("contact").toString());
            //results.add(resultJson.get("pages").toString());
        }
        return results;
    }

    private List<Map<String, String>> getAdditionalData(List<Long> bookIds) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9090/books/additionalInfo/ids/")
                .queryParam("ids", StringUtils.join(bookIds, ','))
                .request(MediaType.APPLICATION_JSON)
                .get();
        List<Map<String,String>> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
        for (Object o : resultObjs) {
            Map<String, String> objectMap = new HashMap<>();
            String jsonResponse = mapper.writeValueAsString(o);
            JSONObject resultJson = createJSONObject(jsonResponse);
            objectMap.put("stockAvailable", resultJson.get("stock").toString());
            objectMap.put("copiesSold", resultJson.get("sold").toString());
            objectMap.put("yearPublished", resultJson.get("year").toString());
            results.add(objectMap);
        }
        return results;
    }

    private List<Map<String, String>> getRestFields(List<Long> bookIds) throws JsonProcessingException {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9090/books/ids/")
                .queryParam("ids", StringUtils.join(bookIds, ','))
                .request(MediaType.APPLICATION_JSON)
                .get();
        List<Map<String, String>> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<Object> resultObjs = response.readEntity(new GenericType<List<Object>>() {});
        for (Object o : resultObjs) {
            //System.out.println(o.toString());
            Map<String, String> objectMap = new HashMap<>();
            String jsonResponse = mapper.writeValueAsString(o);
            JSONObject resultJson = createJSONObject(jsonResponse);
            objectMap.put("countryOfOrigin", resultJson.get("countryOfOrigin").toString());
            //objectMap.put("publisher", resultJson.get("publisher"));
            //objectMap.put("contact", resultJson.get("contact"));
            //objectMap.put("numOfPages", resultJson.get("pages"));
            results.add(objectMap);
        }
        return results;
    }

    public DataFetcher getAllBooksDataFetcher() {
        //return dataFetchingEnvironment -> bookServiceEclipseLink.findAll();
        //return dataFetchingEnvironment -> bookServiceEclipseLink.findAllRecords();
        //return dataFetchingEnvironment -> bookServiceEclipseLink.findAllRecords();
        return dataFetchingEnvironment -> bookServiceEclipseLink.findAllEntries();
    }

    public DataFetcher getAllAuthorsDataFetcher() {
        //return dataFetchingEnvironment -> bookServiceEclipseLink.findAll();
        //return dataFetchingEnvironment -> bookServiceEclipseLink.findAllRecords();
        //return dataFetchingEnvironment -> bookServiceEclipseLink.findAllRecords();
        return dataFetchingEnvironment -> authorServiceEclipseLink.findAllEntries();
    }
    /*
    public DataFetcher getBooksByFilterDataFetcher() {
        return dataFetchingEnvironment -> {
            //printDataFetchingEnv(dataFetchingEnvironment);
            List<Map<String, Object>> result = new ArrayList<>();
            Object filters = dataFetchingEnvironment.getArgument("filters");
            Object pagination = dataFetchingEnvironment.getArgument("pagination");
            Object distinctOn = dataFetchingEnvironment.getArgument("distinctOn");
            Object sortBy = dataFetchingEnvironment.getArgument("sort");
            List<Book> books = bookService.findBookByFilter(filters, pagination, distinctOn, sortBy);
            for (Book book: books) {
                Client client = ClientBuilder.newClient();
                Response response = client.target("http://localhost:9090/books/id/")
                        .queryParam("id", book.getId())
                        .request(MediaType.APPLICATION_JSON)
                        .get();
                result.add(prepareResponsePayload(book, response));
            }
            return result;
        };
    }

    private DataFetcher<?> booksAggregator() {
        return dataFetchingEnvironment -> {
            Object aggregation = dataFetchingEnvironment.getArgument("aggregation");
            return bookService.findAggregation(aggregation).get("result");
        };
    }
     */

    public DataFetcher getAuthorDataFetcherWithDataLoader() {
        return dataFetchingEnvironment -> {
            //printDataFetchingEnv(dataFetchingEnvironment);
            //Book book = dataFetchingEnvironment.getSource();
            Map<String, Object> map = dataFetchingEnvironment.getSource();
            //System.out.println("GetAuthorDataFetcher start");
            //for (Map.Entry<String, Object> m : map.entrySet()) {
            //    System.out.println(m.getKey() + " " + m.getValue());
            //}
            //System.out.println("GetAuthorDataFetcher end");
            Long authorId = Long.parseLong(map.get("authorId").toString());
            //Long authorId = book.getAuthorId();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> authorLoader = dataLoaderRegistry.getDataLoader("authors");
            log.info("Batching request for " + authorId);
            return authorLoader.load(authorId);
        };
    }


    public DataFetcher getAdditionalDetailsDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> authorLoader = dataLoaderRegistry.getDataLoader("additionalDetails");
            return authorLoader.load(book.getId());
        };
    }

    public DataFetcher getCountryDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> restLoader = dataLoaderRegistry.getDataLoader("country");
            return restLoader.load(book.getId());
        };
    }

    public DataFetcher getPublisherDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> restLoader = dataLoaderRegistry.getDataLoader("publisher");
            return restLoader.load(book.getId());
        };
    }

    public DataFetcher getContactDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> restLoader = dataLoaderRegistry.getDataLoader("contact");
            return restLoader.load(book.getId());
        };
    }

    public DataFetcher getPagesDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            DataLoaderRegistry dataLoaderRegistry = dataFetchingEnvironment.getDataLoaderRegistry();
            DataLoader<Long, Author> restLoader = dataLoaderRegistry.getDataLoader("pages");
            return restLoader.load(book.getId());
        };
    }

    public BatchLoader<Long, Author> authorBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    return authorServiceEclipseLink
                            .findByIds(new ArrayList<>(ids));
                });
    }

    public BatchLoader<Long, Map<String, String>> restBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return getRestFields(ids.stream().distinct().collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    public BatchLoader<Long, String> getCountryBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("Sending REST request to get book CountryOfOrigin");
                    try {
                        return getCountryOfOrigin(ids.stream().distinct().collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }
    public BatchLoader<Long, String> getPublisherBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("Sending REST request to get book publisher");
                    try {
                        return getPublisher(ids.stream().distinct().collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    public BatchLoader<Long, String> getContactBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("Sending REST request to get book contact");
                    try {
                        return getContact(ids.stream().distinct().collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    public BatchLoader<Long, String> getPagesBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("Sending REST request to get book pages");
                    try {
                        return getPages(ids.stream().distinct().collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    public BatchLoader<Long, Map<String,String>> getAdditionalDetailsBatchLoader() {
        return ids ->
                CompletableFuture.supplyAsync(() -> {
                    System.out.println("Sending REST request to get book additional data");
                    try {
                        return getAdditionalData(ids.stream().distinct().collect(Collectors.toList()));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    public DataFetcher getAllItemsDataFetcher() {
        return dataFetchingEnvironment -> {
            List<Book> books = bookServiceEclipseLink.findAll();
            List<Author> authors = authorServiceEclipseLink.findAll();
            List<Object> items = new ArrayList<>();
            for (Object o: books)
                items.add(o);
            for (Object o: authors)
                items.add(o);
            for (Object o: items)
                System.out.println(o.toString());
            return items;
        };
    }


    public DataFetcher getItemDataFetcher() {
        return dataFetchingEnvironment -> {
            Long id = Long.parseLong(dataFetchingEnvironment.getArgument("id"));
            Map<String, Object> book = bookServiceEclipseLink.findById(id);
            Map<String, Object> author = authorServiceEclipseLink.findById(id);
            List<Object> result = new ArrayList<>();
            result.add(book); result.add(author);
            return result;
        };
    }

    public GraphQLCodeRegistry.Builder generateBookFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "books"), this.getAllBooksDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "authors"), this.getAllAuthorsDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "book"), this.getBookByIdDataFetcher());
        //codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "booksWithFilter"), this.getBooksByFilterDataFetcher());
        //codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "booksAggregator"), this.booksAggregator());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateAuthorFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        //codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "author"), this.getAuthorDataFetcherWithDataLoader());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "countryOfOrigin"), this.getCountryDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "publisher"), this.getPublisherDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "contact"), this.getContactDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "numOfPages"), this.getPagesDataFetcher());
        return codeRegistryBuilder;
    }

    public TypeResolver generateTypeResolvers() {
        TypeResolver t = typeResolutionEnvironment -> {
            Object javaObject = typeResolutionEnvironment.getObject();
            if (javaObject instanceof Book) {
                return typeResolutionEnvironment.getSchema().getObjectType("Book");
            } else {
                return typeResolutionEnvironment.getSchema().getObjectType("Author");
            }
        };
        return t;
    }

    public GraphQLCodeRegistry.Builder generateInterfaceFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "item"), this.getItemDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "items"), this.getAllItemsDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "inventoryItem"), this.getItemDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "inventory"), this.getAllItemsDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "additionalDetails"), this.getAdditionalDetailsDataFetcher());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateMutationFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Mutation", "createBook"), this.createBook());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Mutation", "updateBook"), this.updateBook());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Mutation", "deleteBook"), this.deleteBook());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateHeroFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "heroes"), this.getHeroDataFetcher());
        return codeRegistryBuilder;
    }

    public GraphQLCodeRegistry.Builder generateFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = generateAuthorFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateBookFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateInterfaceFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateMutationFetchers(codeRegistryBuilder);
        codeRegistryBuilder = generateHeroFetchers(codeRegistryBuilder);
        return codeRegistryBuilder;
    }
}
