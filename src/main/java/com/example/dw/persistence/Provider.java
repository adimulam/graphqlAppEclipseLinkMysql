package com.example.dw.persistence;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Provider {
    private static Provider provider = null;

    private Map<String, EntityManagerFactoryContextWrapper> factories;
    private Map<String, Map<String, String>> typeEntityMapping;  // <ds.name, <objName, tableName>>


    private Provider() {
        factories = new HashMap<>();
        typeEntityMapping = new HashMap<>();
    }

    public static Provider getProvider() {
        // To ensure only one instance is created
        if (provider == null) {
            provider = new Provider();
        }
        return provider;
    }
}
