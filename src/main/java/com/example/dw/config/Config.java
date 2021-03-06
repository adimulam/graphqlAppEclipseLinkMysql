package com.example.dw.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class Config extends Configuration {

    @JsonProperty
    private DbConfig dbConfig;

    public DbConfig getDbConfig() {
        return dbConfig;
    }
}