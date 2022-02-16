package com.example.dw.persistence;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Getter
public class CustomEntityManagerFactory {
//    private static CustomEntityManagerFactory customEntityManagerFactory = null;
//    private Map<String, EntityManagerFactoryContextWrapper> factories;
    private Provider provider;

    private Object factoriesLock;

    public CustomEntityManagerFactory() {
        //factories = new HashMap<>();
        provider = Provider.getProvider();
        factoriesLock = new Object();
    }

//    public static CustomEntityManagerFactory CustomEntityManagerFactory() {
//        // To ensure only one instance is created
//        if (customEntityManagerFactory == null) {
//            customEntityManagerFactory = new CustomEntityManagerFactory();
//        }
//        return customEntityManagerFactory;
//    }

    // get 'DatabaseDataSource datasource' from resolver layer
    public JPADynamicHelper getJPADynamicHelper(DataSource datasource) {
        return getEntityManagerFactory(datasource).getJpaDynamicHelper();
    }

    public EntityManager getEntityManager(DataSource dataSource) {
        return getEntityManagerFactory(dataSource).createEntityManager();
    }

    public void closeEntityManager(DataSource dataSource) {
        getEntityManagerFactory(dataSource).closeEntityManager();
    }

    public EntityManagerFactoryContextWrapper getEntityManagerFactory(DataSource dataSource) {
        log.info("Inside getEntityManagerFactory");
        EntityManagerFactoryContextWrapper emfWrapper = provider.getFactories().get(dataSource.getName());
        if (emfWrapper == null) {
            log.info("post if");
            synchronized (factoriesLock) {
                emfWrapper = provider.getFactories().get(dataSource.getName());
                if (emfWrapper == null) {
                    EntityManagerFactory created = createEntityManagerFactory(dataSource);
                    emfWrapper = new EntityManagerFactoryContextWrapper(created);
                    provider.getFactories().put(dataSource.getName(), emfWrapper);
                }
            }
        }

        return emfWrapper;
    }

    private EntityManagerFactory createEntityManagerFactory(DataSource dataSource) {
        Properties propertiesMap;
        if (dataSource.getType().equals("MYSQL")) {
            propertiesMap = getPropertiesForMySQLType(dataSource);
        }
        else if(dataSource.getType().equals("ATP")) {
            propertiesMap = getPropertiesForOracleDBType(dataSource);
        }
        else {
            throw new RuntimeException("Valid database type must be provided");
        }
        propertiesMap.put("eclipselink.session-name", dataSource.getName());
        propertiesMap.put("eclipselink.cache.shared.default", "false");
        propertiesMap.put("eclipselink.logging.level","FINE");
        propertiesMap.put("eclipselink.logging.level.sql", "FINE");

        ArrayList<String> managedClasses = new ArrayList<>();

        // TODO - change dataSource.getPersistenceUnitName() with proper method call to fetch the
        //  persistence-unit name
        log.info("Creating emf");
        EntityManagerFactory created = new PersistenceProvider()
            .createContainerEntityManagerFactory(new PersistenceUnitInfoImpl(dataSource.getName(),
                                                                             managedClasses,
                                                                             propertiesMap), propertiesMap);
        return created;
    }

    public Properties getPropertiesForMySQLType(DataSource databaseDataSource) {
        // TODO - access the datasource fields as per the common-libs
        // where is URL?
        //DatasourceProperties datasourceProperties = databaseDataSource.getDatasourceProperties();

        //String user = databaseDataSource.getDatasourceProperties().getUsername();
        // TODO - Get the pass from common libs in form of ocid - DONE
        // TODO - Call dp-common-libs to resolve it
        //String password = databaseDataSource.getDatasourceProperties().getPassword();
        String user  = databaseDataSource.getUsername();
        String password = databaseDataSource.getPassword();
        String url = databaseDataSource.getUrl();

        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        try {
            Properties info = new Properties();
            dataSource.setConnectionPoolName(databaseDataSource.getName());
            dataSource.setUser(user);
            dataSource.setPassword(password);
            dataSource.setInitialPoolSize(1);
            dataSource.setConnectionFactoryClassName("com.mysql.jdbc.Driver");
            dataSource.setURL(url);

            dataSource.setInactiveConnectionTimeout(20);
            dataSource.setMaxPoolSize(20);
        } catch (SQLException e){
            throw new RuntimeException(e.getMessage(), e);
        }
        log.info("returning properties");
        Properties properties = new Properties();
        properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);
        return properties;
    }

    public Properties getPropertiesForOracleDBType(DataSource dataSource) {

        log.debug("Retrieving the properties for {}", dataSource.getName());

//        OracleDatabaseProperties oracleDatabaseProperties =
//            ((OracleDatabaseProperties) dataSource.getDatasourceProperties());
//
//        //TODO : Add code to get databasekeystore properties
//        String url = oracleDatabaseProperties.getConnectionString();
//
//        //TODO : We get OCID of vault instead of exact password . So add the code to get password from vault service
//        String password = oracleDatabaseProperties.getPassword();

        Properties properties = new Properties();
        PoolDataSource poolDataSource = PoolDataSourceFactory.getPoolDataSource();
        try {
            Properties info = new Properties();
            info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");
            info.put(OracleConnection.CONNECTION_PROPERTY_FAN_ENABLED, "false");

            //TODO : invoke this convertCall
           /*
           * convertToBcfks(new File("/Users/shashankavunoori/Documents/GraphQL/Virtual Resources - Sample Code/Data Plane/Wallet_RAVIAPUBATP/ewallet.p12"),
                           "OracleAtpJdbc@123".toCharArray());

            */

            //TODO : add exact wallet location
            info.put("javax.net.ssl.keyStore", "/Users/ashitban/Downloads/Wallet_DBforQuerySD/keystore.jks");
            info.put("javax.net.ssl.keyStorePassword", "QuerySupport@1");
            info.put("javax.net.ssl.trustStore", "/Users/ashitban/Downloads/Wallet_DBforQuerySD/truststore.jks");
            info.put("javax.net.ssl.trustStorePassword", "QuerySupport@1");

            poolDataSource.setUser("ADMIN");
            poolDataSource.setPassword("QuerySupport@1");
            poolDataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            poolDataSource.setURL("jdbc:oracle:thin:@(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=f6cjqsbdubnheax_dbforquerysd_high.adb.oraclecloud.com))(security=(ssl_server_cert_dn=\"CN=adwc.uscom-east-1.oraclecloud.com, OU=Oracle BMCS US, O=Oracle Corporation, L=Redwood City, ST=California, C=US\")))");
            poolDataSource.setInactiveConnectionTimeout(60);
            poolDataSource.setMaxPoolSize(4);
            poolDataSource.setValidateConnectionOnBorrow(true);
            poolDataSource.setSQLForValidateConnection("SELECT * from DUAL");
            poolDataSource.setConnectionProperties(info);
        } catch (SQLException e){
            throw new RuntimeException(e.getMessage(), e);
        }
        properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, poolDataSource);

        return properties;
    }

}