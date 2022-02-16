package com.example.dw.persistence;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class QueryWrapper {
    private CustomEntityManagerFactory customEntityManagerFactory;
    private Provider provider;
    private QueryExecution queryExecution;

    public QueryWrapper() {
    }
    public void init(PersistenceService persistenceService) {
        this.customEntityManagerFactory = persistenceService.getCustomEntityManagerFactory();
        this.provider = persistenceService.getCustomEntityManagerFactory().getProvider();
        this.queryExecution = persistenceService.getQueryExecution();
    }

    /**
     * To return a single result from the passed list of object name(s) based on the input object.
     *
     * <p>Method gets a Dynamic helper from the customEntityManagerFactory corresponding to the datasource. The
     * dynamic entity is/are then resolved from the passed object name(s). QueryExecution instance is used to call
     * the helper query methods.</p>
     *
     * @param objectNameList a List of the object name(s) of table on which query has to be constructed.
     * @param dataSource    a DataSource object containing details regarding the datasource
     * @return a DynamicEntity object
     * @see CustomEntityManagerFactory
     */
    public <T> T findALL(List<String> objectNameList, DataSource dataSource) throws SQLException {
        log.debug("invoking queryExecution.queryOne()");
        JPADynamicHelper helper = customEntityManagerFactory.getJPADynamicHelper(dataSource);

        List<Class<T>> entityClass = new ArrayList<>();
        for (String objName : objectNameList) {
            String tableName = provider.getTypeEntityMapping().get(dataSource.getName()).get(objName);
            log.info("typeEntityMapping are {}", provider.getTypeEntityMapping().size());
            entityClass.add(helper.getType(tableName).getDescriptor().getJavaClass());
        }

        return (T) queryExecution.invokeQuery(entityClass, dataSource);
    }
}

