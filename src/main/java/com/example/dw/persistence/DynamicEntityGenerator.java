package com.example.dw.persistence;

import javafx.util.Pair;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.metamodel.MetamodelImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicTypeBuilder;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DynamicEntityGenerator {

    public final String pkgPREFIX = "com.oracle.graphql";

    private CustomEntityManagerFactory customEntityManagerFactory;
    private QueryWrapper queryWrapper;
    private Provider provider;

    public DynamicEntityGenerator() {
    }

    public void init(PersistenceService persistenceService) {
        this.customEntityManagerFactory = persistenceService.getCustomEntityManagerFactory();
        this.provider = customEntityManagerFactory.getProvider();
        this.queryWrapper = persistenceService.getQueryWrapper();
    }

    /*
     * Invoked by the resolver to create dynamic Entites per database datasource
     * @param An EntityModel type object
     * @param A Datasource type object (comes from graphql-common-lib)
     * @returns null
     */
    public void addEntities(List<EntityModel> entityModels, DataSource dataSource) {
        Map<String, JPADynamicTypeBuilder> entityTypeBuilders = new HashMap<>();

        //Add Primary table column mapping
        entityTypeBuilders = addTableColumnMapping(entityModels, dataSource, entityTypeBuilders);

        // TODO: Add secondary relationship or other relationship mapping

        //Add the dynamic types created to helper
        addEntitiesToHelper(entityTypeBuilders, dataSource);
    }

    /*
     * To add the created dynamic Entites per database datasource into JPA Dynamic helper
     * @param null
     * @retun null
     */
    private void addEntitiesToHelper(Map<String,JPADynamicTypeBuilder> entityTypeBuilders, DataSource dataSource) {
        if(entityTypeBuilders.size() == 0) {
            /*
             * TODO : Throw exception
             */
        }
        JPADynamicHelper helper = customEntityManagerFactory.getEntityManagerFactory(dataSource).getJpaDynamicHelper();
        EntityManager entityManager = customEntityManagerFactory.getEntityManager(dataSource);
        // adding relationship between system_user and laptop
        entityTypeBuilders.get("SYSTEM_USER").addOneToOneMapping("laptop", entityTypeBuilders.get("LAPTOP").getType(),"LAPTOP_ID");
        //Add all the dynamic type builders to the jpa helper
        DynamicType[] dynamicTypes = new DynamicType[entityTypeBuilders.size()];
        entityTypeBuilders.values().stream().map(DynamicTypeBuilder::getType).collect(Collectors.toList()).toArray(dynamicTypes);

        helper.addTypes(false,false, dynamicTypes);


        // Refresh the metamodel
        Metamodel metamodel = (Metamodel) new MetamodelImpl((AbstractSession) helper.getSession());
        ((EntityManagerFactoryImpl) JpaHelper.getEntityManagerFactory(entityManager)).setMetamodel(metamodel);
    }

    /*
     * To add primary table column field mapping into the dynamic entity
     * @param entityModels : List of tables of a datasource
     * @param datasource : The datasource object
     * @param entityTypeBuilder : To store created dynamicTypeBuilder createed per table
     * @return entityTypeBuilder
     */
    private Map<String,JPADynamicTypeBuilder> addTableColumnMapping(List<EntityModel> entityModels, DataSource dataSource,
                                                                    Map<String,JPADynamicTypeBuilder> entityTypeBuilders) {
        Map<String, String> objTableMap = new HashMap<>();
        for (EntityModel entityModel : entityModels) {
            objTableMap.put(entityModel.getObjName(), entityModel.getEntityName());
            JPADynamicTypeBuilder dynamicTypeBuilder = createDynamicEntity(entityModel.getEntityName(), dataSource);
            //Add column mapping to entity
            for (Map.Entry<String, Pair<String, String>> entry : entityModel.getColumnMapping().entrySet()) {
                dynamicTypeBuilder.addDirectMapping(entry.getKey(),
                                                    PersistenceConstants.persistenceTypeMap.get(entry.getValue().getValue()),
                                                    entry.getValue().getKey());
            }
            dynamicTypeBuilder.setPrimaryKeyFields(entityModel.getPrimaryKeys().toArray(
                new String[entityModel.getPrimaryKeys().size()]));
            entityTypeBuilders.put(entityModel.getEntityName(), dynamicTypeBuilder);
        }
        provider.getTypeEntityMapping().put(dataSource.getName(), objTableMap);
        return entityTypeBuilders;
    }

    /*
     * To create Dynamic Entity
     * @param Table name
     * @param Datasource type object
     * @return JPADynamicTypeBuilder
     */
    private JPADynamicTypeBuilder createDynamicEntity(String tableName, DataSource dataSource) {
        JPADynamicHelper helper = customEntityManagerFactory.getEntityManagerFactory(dataSource).getJpaDynamicHelper();
        DynamicClassLoader dcl = helper.getDynamicClassLoader();
        Class<?> dynamicEntityClass = dcl.createDynamicClass(pkgPREFIX + dataSource.getName() + '.' + tableName);
        log.debug("Created dynamic class for table {} in datasource {}", tableName, dataSource.getName());
        return new JPADynamicTypeBuilder(dynamicEntityClass, null, tableName);
    }

//    private DatabaseMapping addOnetoOneMapping(JPADynamicHelper helper) {
//        DynamicType sysType = helper.getType("SYS_USER");
//        OneToOneMapping oneToOneMapping = new OneToOneMapping();
//        oneToOneMapping.setAttributeName("laptop");
//        oneToOneMapping.setReferenceClass(helper.getType("LAPTOP").getJavaClass());
//        oneToOneMapping.addTargetForeignKeyFieldName("STUDENT_ID", "ID");
//        return oneToOneMapping;
//
//    }

}
