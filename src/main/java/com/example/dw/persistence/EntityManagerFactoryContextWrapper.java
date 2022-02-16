package com.example.dw.persistence;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.jpa.dynamic.JPADynamicHelper;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class EntityManagerFactoryContextWrapper implements EntityManagerFactory {
    private static final ThreadLocal<EntityManager> threadLocal = new ThreadLocal<>();

    private final EntityManagerFactory delegate;
    private JPADynamicHelper jpaDynamicHelper;

    public EntityManagerFactoryContextWrapper(EntityManagerFactory delegate) {
        this.delegate = delegate;
        // there is no need to create a new helper every time - they are all identical
        jpaDynamicHelper = new JPADynamicHelper(delegate);
    }

    public EntityManagerFactory getDelegate() {
        return delegate;
    }

    public JPADynamicHelper getJpaDynamicHelper() {
    	if (jpaDynamicHelper != null) {
    		return jpaDynamicHelper;
    	} else {
    		JPADynamicHelper newHelper = new JPADynamicHelper(delegate);
    		jpaDynamicHelper = newHelper;
    		return newHelper;
    	}
    }

    @Override
    public EntityManager createEntityManager() {
        return createEntityManagerWrappedWithContext(EntityManagerFactory::createEntityManager);
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return createEntityManagerWrappedWithContext((e) -> e.createEntityManager(map));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createEntityManagerWrappedWithContext((e) -> e.createEntityManager(synchronizationType));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return createEntityManagerWrappedWithContext((e) -> e.createEntityManager(synchronizationType, map));
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
        if (!delegate.isOpen()) {
            jpaDynamicHelper = null;
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        delegate.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return delegate.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        delegate.addNamedEntityGraph(graphName, entityGraph);
    }

    public static void resetThreadLocal() {
        Optional.ofNullable(threadLocal.get()).ifPresent(e -> threadLocal.set(null));
    }

    public void beginTransaction() {
        createEntityManager().getTransaction().begin();
    }

    public void closeTransaction(Throwable err) {
        EntityTransaction txn = createEntityManager().getTransaction();
        if (err != null) {
            txn.rollback();
        } else {
            txn.commit();
        }
    }

    private EntityManager createEntityManagerWrappedWithContext(Function<EntityManagerFactory, EntityManager> fn) {
        EntityManager em = threadLocal.get();
        if (em == null) {
            em = fn.apply(delegate);
            log.info("crossed delegate");
            threadLocal.set(em);
        }
        log.info("Trying to return em");
        return em;
    }

    public void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            resetThreadLocal();
        }
    }
}
