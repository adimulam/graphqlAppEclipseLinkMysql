package com.example.dw.provider;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.example.dw.entity.Author;
import com.example.dw.datafetchers.GraphQLDataFetcher;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class GraphQLProvider {

    @Inject
    private GraphQLDataFetcher graphQLDataFetchers;

    private DataLoaderRegistry dataLoaderRegistry;

    private GraphQL graphQL;

    public GraphQLProvider() {
    }

    @Inject
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
        this.registerDataLoaders();
    }

    private void registerDataLoaders() {
        dataLoaderRegistry = new DataLoaderRegistry();
        DataLoader<Long, Author> authorDataLoader = DataLoaderFactory
                .newDataLoader(graphQLDataFetchers.authorBatchLoader());
        dataLoaderRegistry.register("authors", authorDataLoader);
        log.info("Registered data loaders");
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("books", graphQLDataFetchers.getAllBooksDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Book")
                        //.dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcherWithDataLoader()))
                .build();
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
}