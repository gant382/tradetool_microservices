package com.saicon.games.callcard.entity.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

/**
 * Hibernate UUID generator for CallCard entities.
 * Generates uppercase UUID strings compatible with SQL Server uniqueidentifier type.
 *
 * Adapted from gameserver_v3 UUIDGenerator for microservice use.
 */
public class UUIDGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return UUID.randomUUID().toString().toUpperCase();
    }
}
