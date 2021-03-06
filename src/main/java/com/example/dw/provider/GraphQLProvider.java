package com.example.dw.provider;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.example.dw.entity.Author;
import com.example.dw.datafetchers.GraphQLDataFetcher;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions;
import graphql.language.*;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import graphql.schema.idl.*;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class GraphQLProvider {

    @Inject
    private GraphQLDataFetcher graphQLDataFetchers;

    private DataLoaderRegistry dataLoaderRegistry;

    private GraphQL graphQL;
    private ObjectTypeDefinition typeDefinition;

    public GraphQLProvider() {
    }

    @Inject
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        DataLoaderDispatcherInstrumentationOptions options = DataLoaderDispatcherInstrumentationOptions
                .newOptions().includeStatistics(true);
        DataLoaderDispatcherInstrumentation dispatcherInstrumentation
                = new DataLoaderDispatcherInstrumentation(options);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(dispatcherInstrumentation)
                .build();
        //this.registerDataLoaders();
    }

    private void registerDataLoaders() {
        dataLoaderRegistry = new DataLoaderRegistry();
        DataLoaderOptions options = new DataLoaderOptions().setCachingEnabled(Boolean.TRUE);
        DataLoader<Long, Author> authorDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.authorBatchLoader(), options);
        dataLoaderRegistry.register("authors", authorDataLoader);

        DataLoader<Long, String> countryDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.getCountryBatchLoader(), options);
        dataLoaderRegistry.register("country", countryDataLoader);

        DataLoader<Long, String> publisherDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.getPublisherBatchLoader(), options);
        dataLoaderRegistry.register("publisher", publisherDataLoader);

        DataLoader<Long, String> contactDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.getContactBatchLoader(), options);
        dataLoaderRegistry.register("contact", contactDataLoader);

        DataLoader<Long, String> pagesDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.getPagesBatchLoader(), options);
        dataLoaderRegistry.register("pages", pagesDataLoader);

        DataLoader<Long, Map<String,String>> additionalDetailsDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.getAdditionalDetailsBatchLoader(), options);
        dataLoaderRegistry.register("additionalDetails", additionalDetailsDataLoader);

        log.info("Registered data loaders");
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        /*
        List<GraphQLDataFetcher.GraphQLSchemaType> typeList = new ArrayList<>();
        Map<java.lang.String, TypeDefinition> list = typeRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : list.entrySet()) {
            System.out.println(entry.getValue());
            System.out.println();
            if (entry.getValue() instanceof ObjectTypeDefinition) {
                GraphQLDataFetcher.GraphQLSchemaType graphqlSchemaType = new GraphQLDataFetcher.GraphQLSchemaType();
                List<String> fieldList = new ArrayList<>();
                graphqlSchemaType.setType(entry.getValue().getName());
                for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
                    fieldList.add(fieldDef.getName());
                }
                graphqlSchemaType.setFieldList(fieldList);
                typeList.add(graphqlSchemaType);
            }
        }
        for (GraphQLDataFetcher.GraphQLSchemaType type: typeList) {
            System.out.println(type.getType());
            System.out.println(type.getFieldList());
        }

        types.forEach((key, value) -> {
            System.out.println();
            System.out.println(key+" : "+ value);
            System.out.println(value.getChildren().get(0));
            System.out.println();
        });

        System.out.println(typeRegistry.types().keySet());
        for (String type: typeRegistry.types().keySet()) {
            if (type.toLowerCase(Locale.ROOT).equals("query")) {
                System.out.println(type);
                System.out.println(typeRegistry.getType(type).get());
            }
        }
        */
        RuntimeWiring runtimeWiring = buildWiring(typeRegistry);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    /*
    private GraphQLCodeRegistry.Builder generateBookFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "books"), graphQLDataFetchers.getAllBooksDataFetcher());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "bookById"), graphQLDataFetchers.getBookByIdDataFetcher());
        return codeRegistryBuilder;
    }

    private GraphQLCodeRegistry.Builder generateAuthorFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Book", "author"), graphQLDataFetchers.getAuthorDataFetcherWithDataLoader());
        codeRegistryBuilder = codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates("Query", "authors"), graphQLDataFetchers.getAllAuthorsDataFetcher());
        return codeRegistryBuilder;
    }
    */

    private static String getType(Type<?> type) {
        if (type instanceof TypeName) {
            return (((TypeName) type).getName());
        }
        if (type instanceof NonNullType) {
            return getType(((NonNullType) type).getType());
        }
        if (type instanceof ListType) {
            return getType(((ListType) type).getType());
        }
        return null;
    }

    private RuntimeWiring buildWiring(TypeDefinitionRegistry registry) {
        for (ObjectTypeDefinition typeDefinition : registry.getTypes(ObjectTypeDefinition.class)) {
            if ("Query".equals(typeDefinition.getName())) {
                for (FieldDefinition fieldDefinition : typeDefinition.getFieldDefinitions()) {
                    String fieldName = fieldDefinition.getName();
                    boolean isMultiple = fieldDefinition.getType() instanceof ListType;
                    String objectType = getType(fieldDefinition.getType());
                    //System.out.println(fieldName + '\t' + isMultiple + '\t' + objectType);
                    //System.out.println();
                }
            }
        }

        /*
        //System.out.println(typeDefinitionRegistry.getType("Query").get());
        if (registry.getType("Query").isPresent()) {
            TypeDefinition queries = registry.getType("Query").get();
            //System.out.println(queries);
            //System.out.println(queries.getChildren());
            System.out.println(queries.getChildren().getClass());
            List<Node> children = queries.getChildren();
            for (Node child : children) {
                System.out.println(child);
                FieldDefinition fieldDefinition = child.getFieldDefinitions();

            }
            //System.out.println(queries.getChildren().get(0));
         }
         */

        /*
        return RuntimeWiring.newRuntimeWiring()
                .type("AllItems", typeWriting -> typeWriting.typeResolver(t))
                .build();
         */

        // Method-1
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
        codeRegistryBuilder = graphQLDataFetchers.generateFetchers(codeRegistryBuilder);
        codeRegistryBuilder = graphQLDataFetchers.generateMutationFetchers(codeRegistryBuilder);
        TypeResolver typeResolver = graphQLDataFetchers.generateTypeResolvers();

        return RuntimeWiring.newRuntimeWiring()
                .codeRegistry(codeRegistryBuilder)
                .type("Item", typeWriting -> typeWriting.typeResolver(typeResolver))
                .type("AllItems", typeWriting -> typeWriting.typeResolver(typeResolver))
                .build();

        // Method-2
        /*
        GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry
                .newCodeRegistry()
                .dataFetcher(FieldCoordinates.coordinates("Query", "books"), graphQLDataFetchers.getAllBooksDataFetcher())
                .dataFetcher(FieldCoordinates.coordinates("Query", "bookById"), graphQLDataFetchers.getBookByIdDataFetcher())
                .dataFetcher(FieldCoordinates.coordinates("Book", "author"), graphQLDataFetchers.getAuthorDataFetcherWithDataLoader())
                .build();
        return RuntimeWiring.newRuntimeWiring().codeRegistry(codeRegistry).build();
        */

        // Method-3
        /*
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("books", graphQLDataFetchers.getAllBooksDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("authors", graphQLDataFetchers.getAllAuthorsDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("bookByName", graphQLDataFetchers.getBookByNameDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Book")
                        //.dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcherWithDataLoader()))
                .build();
         */
    }

    public GraphQL graphQL() {
        return graphQL;
    }

    public Object invoke(String query) {
        log.info("Invoking GraphQL Query");
        if (dataLoaderRegistry != null) {
            log.info("Using Registered Data Loaders");
        } else {
            log.info("No Registered Data Loaders Found!!");
        }
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .dataLoaderRegistry(dataLoaderRegistry)
                .build();
        return graphQL().execute(executionInput);
    }

    public Object invokePerRequest(String query) {
        this.registerDataLoaders();
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .dataLoaderRegistry(dataLoaderRegistry)
                .build();
        return graphQL().execute(executionInput);
    }
}