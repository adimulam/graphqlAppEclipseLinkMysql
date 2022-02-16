package com.example.dw.persistence;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.jpa.PersistenceProvider;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    public static final String JPA_VERSION = "2.1";

    private static final String PERSISTENCE_UNIT_ROOT = "persistence";

    private final String persistenceUnitName;

    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

    private final List<String> managedClassNames;

    private final List<String> mappingFileNames = new ArrayList<>();

    private final Properties properties;

    private DataSource jtaDataSource;

    private DataSource nonJtaDataSource;

    public PersistenceUnitInfoImpl(String persistenceUnitName, List<String> managedClassNames, Properties properties) {
        this.persistenceUnitName = persistenceUnitName;
        this.managedClassNames = managedClassNames;
        this.properties = properties;
    }


    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return PersistenceProvider.class.getName();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }


    @Override
    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return Collections.emptyList();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        try {
            File persistenceUnitRootLocation = new File(PERSISTENCE_UNIT_ROOT);
            if (!persistenceUnitRootLocation.exists()) {
                boolean created = persistenceUnitRootLocation.mkdir();
                if (!created) {
                    throw new RuntimeException("Failed to create persistence unit root directory");
                }
            }
            return persistenceUnitRootLocation.toURI().toURL();
        } catch (MalformedURLException e) {

        }
        return null;
    }

    @Override
    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.UNSPECIFIED;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.AUTO;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return JPA_VERSION;
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {

    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }
}
