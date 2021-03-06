package com.example.dw.dao;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

@Transactional
public class DaoImpl implements Dao {

    private static final String QUERY_SELECT_ALL = "SELECT o FROM %s o ORDER BY o.id";

    private final Provider<EntityManager> entityManager;

    @Inject
    public DaoImpl(final Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> T persist(final T object) {
        entityManager.get().persist(object);
        entityManager.get().flush();
        return object;
    }

    @Override
    public <T, ID> T findById(final Class<T> clazz, final ID id) {
        return entityManager.get().find(clazz, id);
    }

    @Override
    public <T> T merge(final T object) {
        return entityManager.get().merge(object);
    }

    @Override
    public <T> void remove(final T object) {
        entityManager.get().remove(object);
    }

    @Override
    public <T, ID> void removeById(final Class<T> clazz, final ID id) {
        remove(findById(clazz, id));
    }

    @Override
    public <T> List<T> findAll(final Class clazz) {
        final String query = String.format(QUERY_SELECT_ALL, clazz.getSimpleName());
        return entityManager.get().createQuery(query).getResultList();
    }

    @Override
    public <T> List<T> find(final Class<T> clazz, final String namedQuery,
                            final Map<String, Object> paramsMap, int limit) {
        final Query query = fillNamedParametersQuery(clazz, namedQuery, paramsMap);
        if (limit > 0) {
            return query.setMaxResults(limit).getResultList();
        }
        return query.getResultList();
    }

    private Query fillNamedParametersQuery(final Class clazz, final String namedQuery,
                                           final Map<String, Object> paramsMap) {
        final Query query = entityManager.get().createNamedQuery(namedQuery, clazz);
        paramsMap.entrySet().forEach((param) -> query.setParameter(param.getKey(), param.getValue()));
        return query;
    }

    @Override
    public <T> float findAggregate(final Class<T> clazz, final String namedQuery) {
        float result = ((Number)entityManager.get().createNamedQuery(namedQuery).getSingleResult()).floatValue();
        return result;
    }

    @Override
    public <T> List<T> findWithFilter(Class<T> clazz, String namedQuery) {
        return entityManager.get().createNamedQuery(namedQuery).getResultList();
    }

}