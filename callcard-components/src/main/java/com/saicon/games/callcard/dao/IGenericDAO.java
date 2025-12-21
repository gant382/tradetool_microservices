package com.saicon.games.callcard.dao;

import java.io.Serializable;
import java.util.List;

/**
 * Generic DAO interface for basic CRUD operations.
 *
 * @param <T> Entity type
 * @param <PK> Primary key type
 * @author CallCard Microservice
 */
public interface IGenericDAO<T, PK extends Serializable> {

    /**
     * Persist the newInstance object into database
     */
    void create(T newInstance);

    /**
     * Retrieve an object that was previously persisted to the database using
     * the indicated id as primary key. If can not be found returns null.
     *
     * @param id the primary key of the object to be retrieved
     * @return the persisted object or null if can not be found
     */
    T read(PK id);

    /**
     * Get a reference to an entity instance. No query is made to the db by this call so the instance's state is not populated.
     * @param id the primary key of the entity instance
     * @return reference to the entity instance
     */
    T getReference(PK id);

    /**
     * Save changes made to a persistent object.
     */
    void update(T transientObject);

    /**
     * Remove an object from persistent storage in the database
     */
    void delete(T persistentObject);

    /**
     * List all items in the table
     */
    List<T> listAll();

    /**
     * Execute the query identified by {@code queryName} and return a list of T items.
     * WARNING: this method prefixes the queryName with the fully-qualified name
     * of the type T.
     */
    List<T> queryList(String queryName, Object... args);

    /**
     * Executes the query identified by {@code queryName} and returns a list of
     * {@code T} items. The maximum number of items to be returned is {@code maxResults}.
     * As all {@code query*} methods in this interface, this method prefixes the
     * {@code queryName} with the fully-qualified name of type T.
     *
     * @param queryName  the name of the query
     * @param fromIndex  0-based index of result to start fetching results from
     * @param maxResults maximum number of results to be returned
     * @param args       parameters to the query
     * @return List of {@code T} objects with a maximum of {@code maxResults} items
     */
    List<T> queryListRange(String queryName, int fromIndex, int maxResults, Object... args);

    /**
     * Executes the native SQL query identified by {@code query} and returns a list of
     * {@code T} items.
     */
    List<T> nativeQueryList(String query, Class classType);

    /**
     * Executes the native SQL query identified by {@code query} and returns a list of
     *
     * @param query     the query to execute
     * @param classType the return class type
     * @param args      the list of arguments to pass in the query
     * @return a list of {@code T} items.
     */
    List<T> nativeQueryList(String query, Class classType, Object... args);

    /**
     * Executes the native SQL query identified by {@code query} and returns a list of
     * {@code T} items for a range of items.
     */
    List<T> nativeQueryListRange(String query, int fromIndex, int maxResults, Class classType);

    /**
     * Execute the query identified by {@code queryName} and return a single result of type T.
     * WARNING: this method prefixes the queryName with the fully-qualified name
     * of the type T.
     *
     * @param queryName the query name. This name is prefixed with the FQN of the entity class
     * @param args      the arguments to be supplied in the named query
     * @return the DB entry T
     * @throws javax.persistence.NoResultException
     *          thrown if no DB entry could be retrieved
     * @throws javax.persistence.NonUniqueResultException
     *          thrown if more than on records could be found
     */
    T querySingle(String queryName, Object... args);

    /**
     * Returns a list of objects
     */
    List queryObjects(String queryName, Object... args);

    List nativeQueryObjectsRange(String query, int fromIndex, int maxResults);

    /**
     * Does the same as queryListRange with the only difference that it returns a list that contains
     * elements of type Object and not of type T.
     */
    List queryObjectsRange(String queryName, int fromIndex, int maxResults, Object... args);

    /**
     * Execute the native SQL query identified by {@code query} and return a single result of type T.
     */
    T nativeQuerySingle(String query, Class classType);

    /**
     * Executive native SQL query and return a single number result (for example a COUNT query).
     * May return {@code null}, in case the entity manager cannot create the query.
     */
    Number nativeQueryNumber(String query);

    /**
     * Executive native SQL query and return a single number result (for example a COUNT query).
     * May return {@code null}, in case the entity manager cannot create the query.
     *
     * @param query the native query to execute
     * @param args  the arguments to be used for the query
     */
    Number nativeQueryNumber(String query, Object... args);

    /**
     * Execute a query with the given name and the given arguments. The result is a single object (ex. count query, sum)
     *
     * @param queryName the query name
     * @param args      the query arguments
     * @return the number calculated
     */
    Number queryNumber(String queryName, Object... args);

    /**
     * Execute an update or delete named query.
     *
     * @param queryName the query name
     * @param args the query arguments
     * @return the number of entities affected
     */
    int executeUpdate(String queryName, Object... args);

    void flush();

    void evictCachedEntity(PK primaryKey, String queryRegionName);

    int nativeExecuteUpdate(String query, Object... args);
}
