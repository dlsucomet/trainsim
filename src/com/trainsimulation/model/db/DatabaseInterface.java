package com.trainsimulation.model.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// An object containing database-related functionality
public class DatabaseInterface {

    private final SessionFactory sessionFactory;

    public DatabaseInterface() throws Throwable {
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    Session createSession() {
        return this.sessionFactory.openSession();
    }
}
