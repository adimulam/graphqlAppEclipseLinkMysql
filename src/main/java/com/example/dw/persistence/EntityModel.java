package com.example.dw.persistence;

import javafx.util.Pair;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class EntityModel {
    private String entityName;
    private String objName;
    private Set<String> primaryKeys;
    private Map<String, Pair<String, String>> columnMapping;      // <fieldName,<column, type>>
    // private Map<relationship>

    public EntityModel(String entityName, String objName, Set<String> primaryKeys, Map<String, Pair<String, String>> columnMapping) {
        this.entityName = entityName;
        this.objName = objName;
        this.primaryKeys = primaryKeys;
        this.columnMapping = columnMapping;
    }

}
