package com.example.dw.persistence;

import lombok.Getter;

@Getter
public class PersistenceService {
    private CustomEntityManagerFactory  customEntityManagerFactory;
    private DynamicEntityGenerator dynamicEntityGenerator;
    private QueryExecution queryExecution;
    private QueryWrapper queryWrapper;

    public PersistenceService() {
        customEntityManagerFactory = new CustomEntityManagerFactory();
        dynamicEntityGenerator = new DynamicEntityGenerator();
        queryExecution = new QueryExecution();
        queryWrapper = new QueryWrapper();
    }

    public void init() {
        dynamicEntityGenerator.init(this);
        queryWrapper.init(this);
        queryExecution.init(this);
    }

    public PersistenceService getObject() {
        return  this;
    }
}
