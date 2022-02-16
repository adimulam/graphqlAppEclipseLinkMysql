package com.example.dw.persistence;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oracle.ucp.jdbc.PoolDataSource;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class QueryExecution {

    private CustomEntityManagerFactory customEntityManagerFactory;
    private Provider provider;

    public QueryExecution() {
    }

    public void init(PersistenceService persistenceService) {
        this.customEntityManagerFactory = persistenceService.getCustomEntityManagerFactory();
        this.provider = persistenceService.getCustomEntityManagerFactory().getProvider();
    }

//    public  void checkConnection(DataSource dataSource) throws SQLException {
//       Map<String, Object> properties = customEntityManagerFactory.getEntityManagerFactory(dataSource).getProperties();
//       PoolDataSource dataSource1 = ((PoolDataSource) properties.get(PersistenceUnitProperties.NON_JTA_DATASOURCE));
//       log.info("datasource1 connection pool name {}",dataSource1.getConnectionPoolName());
//       dataSource1.setConnectionPoolName("definedHUman");
//       properties.set(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource1);
//
//
//    }
    public <T> List<T> invokeQuery(List<Class<T>> entityClass, DataSource dataSource) throws SQLException {

        log.info("Beginning query execution");
        //String tableName = provider.getTypeEntityMapping().get(dataSource.getName()).get(objName);
        //JPADynamicHelper helper = customEntityManagerFactory.getJPADynamicHelper(dataSource);
       // Class<T> entityClass =  helper.getType(tableName).getDescriptor().getJavaClass();
        //log.info("entityClass is {}", entityClass.getName().);
        //EntityManagerFactory entityManagerFactory = customEntityManagerFactory.getEntityManagerFactory(dataSource);
        EntityManager entityManager = customEntityManagerFactory.getEntityManager(dataSource);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<T> cq = cb.createQuery(entityClass.get(0));
        Root<T> root = cq.from(entityClass.get(0));
        root.fetch("laptop", JoinType.LEFT);
       // root.join("laptop", JoinType.LEFT);
        //cq.select(root);
//        Predicate p = cb.equal(root.get("name"), "Bob");
//        cq.where(p);
        TypedQuery<T> q = entityManager.createQuery(cq);
        List<T> resultList = q.getResultList();
        List<DynamicEntity> res = (List<DynamicEntity>) resultList;
        for (DynamicEntity entity : res) {
            log.info(entity.get("sid").toString());
            log.info(entity.get("firstName"));
            log.info(entity.get("lastName"));
            DynamicEntity lap = entity.get("laptop");
            if( null != lap) {
                log.info(lap.get("model"));
                log.info(lap.get("disk").toString());
                log.info(lap.get("lapid").toString());  // spins a new query SELECT LID, DATA, MODEL, STUDENT_ID FROM LAPTOP WHERE (LID = ?) if fetch or Join not used
            }

            log.info("============================================");
        }
//        List<HashMap<String, Object>> ret = new ArrayList<>();
//        HashMap<String, String> schemaFieldToColumnMap = new HashMap<String, String>() { {
//            put("gid" , "ID");
//            put("name", "NAME");
//            put("city", "CITY");
//            put("pin", "PIN");
//            put("gradDate", "GRADUATION");
//            put("admitted", "ADMITTED");
//        }};
//        for (DynamicEntity entity : res) {
//            HashMap<String, Object> thisEntry = new HashMap<>();
//            thisEntry.put("___SYSTEM___.objectType", "Student");
//            //populate by schema field in case the columns are mapped to multiple fields
//            for (Map.Entry<String, String> schemaEntry : schemaFieldToColumnMap.entrySet()) {
//                String schemaField = schemaEntry.getKey();
//                String columnName = schemaEntry.getValue();
//                if (entity.isSet(schemaField)) {
//                    thisEntry.put(schemaField, entity.get(schemaField));
//                }
//                log.info("values are field {} col {} value {} ", schemaField, columnName, entity.get(schemaField));
//            }
//            ret.add(thisEntry);
//        }

        log.info("result {}", resultList.size());
        return resultList;
    }
}
