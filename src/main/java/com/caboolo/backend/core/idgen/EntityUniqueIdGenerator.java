package com.caboolo.backend.core.idgen;

import jakarta.annotation.PostConstruct;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class EntityUniqueIdGenerator implements IdentifierGenerator  {
    private static EntityUniqueIdGenerator instance;

    @Autowired
    private SequenceGenerator tokenGenerationService;

    @PostConstruct
    public void init() {
        instance = this;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object o) throws HibernateException {
        // Return existing ID if set, otherwise generate new snowflake ID
        Object id = session.getEntityPersister(null, o).getIdentifier(o, session);
        return id != null ? id : instance.tokenGenerationService.nextId();
    }
}
