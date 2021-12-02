package com.example.dw.dataloader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoaderRegistry;

@Slf4j
@Getter
public class GraphQLDataLoaderRegistry {

    private DataLoaderRegistry dataLoaderRegistry;

    public GraphQLDataLoaderRegistry(DataLoaderRegistry dataLoaderRegistry) {
        this.dataLoaderRegistry = dataLoaderRegistry;
    }
}
