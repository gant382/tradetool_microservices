package com.saicon.games.callcard.dao;

import com.saicon.games.callcard.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic DAO implementation providing basic CRUD operations for entities.
 * Adapted for CallCard microservice from gameserver_v3 GenericDAOImpl.
 *
 * @param <T> Entity type
 * @param <PK> Primary key type
 */
public class GenericDAO<T, PK extends Serializable> implements IGenericDAO<T, PK> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericDAO.class);

    private final Class<T> type;
    private final EntityManager entityManager;

    public GenericDAO(Class<T> type, EntityManager entityManager) {
        this.type = type;
        this.entityManager = entityManager;
    }

    @Override
    public void create(T newInstance) {
        entityManager.persist(newInstance);
    }

    @Override
    public T read(PK id) {
        return entityManager.find(type, id);
    }

    @Override
    public T getReference(PK id) {
        return entityManager.getReference(type, id);
    }

    @Override
    public void update(T transientObject) {
        entityManager.merge(transientObject);
    }

    @Override
    public void delete(T persistentObject) {
        entityManager.remove(persistentObject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> listAll() {
        String queryName = type.getName() + ".listAll";

        Query q = findQuery(queryName);

        if (q == null) {
            queryName = type.getSimpleName() + ".listAll";
            q = findQuery(queryName);
        }

        Assert.notNull(q, "Could not find named query: " + queryName);

        List<T> retList = null;
        if (q != null) {
            retList = q.getResultList();
        }

        return retList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> queryList(String queryName, Object... args) {
        Query q = createQuery(queryName, args);
        if (q == null) {
            LOGGER.error("Could not find query with name: {}", queryName);
            return new ArrayList<>();
        }
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> queryListRange(String queryName, int fromIndex, int maxResults, Object... args) {
        return queryObjectsRange(queryName, fromIndex, maxResults, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> nativeQueryList(String query, Class classType) {
        Query q = entityManager.createNativeQuery(query, classType);
        if (q == null) {
            LOGGER.error("Could not create native query: {}", query);
            return null;
        }
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> nativeQueryList(String query, Class classType, Object... args) {
        Query q = entityManager.createNativeQuery(query, classType);
        if (q == null) {
            LOGGER.error("Could not create native query: {}", query);
            return null;
        }

        if (args != null && args.length > 0) {
            int pos = 1;
            for (Object o : args) {
                q.setParameter(pos++, o);
            }
        }

        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> nativeQueryListRange(String query, int fromIndex, int maxResults, Class classType) {
        Query q = entityManager.createNativeQuery(query, classType);
        if (q != null) {
            q.setMaxResults(maxResults);
            q.setFirstResult(fromIndex);
        } else {
            LOGGER.error("Could not create native query: {}", query);
            return null;
        }
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T querySingle(String queryName, Object... args) {
        Query q = createQuery(queryName, args);
        if (q == null) {
            LOGGER.error("Could not find query with name: {}", queryName);
            return null;
        }
        return (T) q.getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List queryObjects(String queryName, Object... args) {
        Query q = createQuery(queryName, args);
        if (q == null) {
            LOGGER.error("Could not find query with name: {}", queryName);
            return null;
        }
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List nativeQueryObjectsRange(String query, int fromIndex, int maxResults) {
        Query q = entityManager.createNativeQuery(query);
        if (q != null) {
            q.setMaxResults(maxResults);
            q.setFirstResult(fromIndex);
        } else {
            LOGGER.error("Could not create native query: {}", query);
            return null;
        }
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List queryObjectsRange(String queryName, int fromIndex, int maxResults, Object... args) {
        Query q = createQuery(queryName, args);
        if (q != null) {
            if (maxResults > 0) {
                q.setMaxResults(maxResults);
            }
            if (fromIndex > 0) {
                q.setFirstResult(fromIndex);
            }
        } else {
            LOGGER.error("Could not find query with name: {}", queryName);
            return null;
        }
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T nativeQuerySingle(String query, Class classType) {
        Query q = entityManager.createNativeQuery(query, classType);
        if (q == null) {
            LOGGER.error("Could not create native query: {}", query);
            return null;
        }
        return (T) q.getSingleResult();
    }

    @Override
    public Number nativeQueryNumber(String query) {
        Query q = entityManager.createNativeQuery(query);
        if (q != null) {
            return (Number) q.getSingleResult();
        } else {
            LOGGER.error("Could not create native query: {}", query);
            return null;
        }
    }

    @Override
    public Number nativeQueryNumber(String query, Object... args) {
        Query q = entityManager.createNativeQuery(query);
        if (q != null) {
            if (args != null && args.length > 0) {
                int pos = 1;
                for (Object o : args) {
                    q.setParameter(pos++, o);
                }
            }
            return (Number) q.getSingleResult();
        } else {
            return null;
        }
    }

    @Override
    public Number queryNumber(String queryName, Object... args) {
        Query q = createQuery(queryName, args);
        if (q == null) {
            LOGGER.error("Could not find query with name: {}", queryName);
            return null;
        }

        Object o = q.getSingleResult();
        return o != null ? (Number) o : null;
    }

    @Override
    public int executeUpdate(String queryName, Object... args) {
        Query q = createQuery(queryName, args);
        if (q == null) {
            LOGGER.error("Could not find query with name: {}", queryName);
            return -1;
        }
        return q.executeUpdate();
    }

    @Override
    public int nativeExecuteUpdate(String query, Object... args) {
        Query q = entityManager.createNativeQuery(query);
        if (q != null) {
            if (args != null && args.length > 0) {
                int pos = 1;
                for (Object o : args) {
                    q.setParameter(pos++, o);
                }
            }
        } else {
            LOGGER.error("Could not create native query: {}", query);
            return -1;
        }

        return q.executeUpdate();
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public void evictCachedEntity(PK primaryKey, String queryRegionName) {
        entityManager.getEntityManagerFactory().getCache().evict(type, primaryKey);

        if (queryRegionName != null) {
            Session session = (Session) entityManager.getDelegate();
            session.getSessionFactory().getCache().evictQueryRegion(queryRegionName);
        }
    }

    // ============================================================
    // Private Helper Methods
    // ============================================================

    /**
     * Finds a named query in the entity manager.
     *
     * @param queryName the query name to search for
     * @return the Query if found, otherwise returns null
     */
    private Query findQuery(String queryName) {
        try {
            return entityManager.createNamedQuery(queryName);
        } catch (IllegalArgumentException e) {
            // Query not found
            return null;
        }
    }

    /**
     * Creates a query with the given name and binds the provided arguments.
     *
     * @param queryName the name of the query
     * @param args      the arguments needed for the query execution
     * @return the query ready for execution if found, otherwise returns null
     */
    private Query createQuery(String queryName, Object... args) {
        String fullQueryName = type.getName() + "." + queryName;

        Query q = findQuery(fullQueryName);

        if (q == null) {
            fullQueryName = type.getSimpleName() + "." + queryName;
            q = findQuery(fullQueryName);
        }

        if (q == null) {
            LOGGER.warn("Could not find named query: {}", queryName);
            return null;
        }

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                q.setParameter(i + 1, args[i]);
            }
        }

        return q;
    }
}
