package com.dozortsev.bookstore.data;

import com.dozortsev.bookstore.model.AbstractEntity;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.log4j.Logger.getLogger;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@SuppressWarnings("unchecked")
@Transactional(rollbackFor = Throwable.class, propagation = REQUIRES_NEW)
@Repository
public class BaseRepoImpl<ID extends Serializable, T extends AbstractEntity<ID>> implements BaseRepo<ID, T> {

    private T t;

    private Class<T> clazz;

    private SessionFactory factory;

    private static final Logger log = getLogger(BaseRepoImpl.class);

    @Autowired
    public void setFactory(SessionFactory factory) {
        this.factory = factory;
    }

    Session getSession() {
        return factory.getCurrentSession();
    }

    @Override
    public ID save(T t) {
        try {
            log.info(format("Saving new %s.", getEntityName()));
            getSession().save(t);
            log.info("Successful saved.");

        } catch (HibernateException e) {
            log.error("Error:", e);
        }
        return t.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public T load(ID id) {
        try {
            log.info(format("Loading %s by Id: %s.", getEntityName(), id));
            t = (T) getSession().get(getEntityClass(), id);
            log.info("Successful loaded.");

        } catch (HibernateException e) {
            log.error("Error:", e);
        }
        return t;
    }

    @Override
    public void delete(T t) {
        try {
            log.info(format("Deleting %s.", getEntityName()));
            getSession().delete(t);
            log.info("Successful deleted.");

        } catch (HibernateException e) {
            log.error("Error:", e);
        }
    }

    @Override
    public void deleteById(ID id) {
        try {
            log.info(format("Deleting %s by Id: %s.", getEntityName(), id));
            t = (T) getSession().get(getEntityClass(), id);

            if (t != null) {
                log.info(format("%s successful loaded.", getEntityName()));
                getSession().delete(t);
                log.info("Successful deleted.");
                return;
            }
            log.info(format("%s not loaded.", getEntityName()));

        } catch (HibernateException e) {
            log.error("Error:", e);
        }
    }

    @Override
    public T update(T t) {
        try {
            log.info(format("Updating %s.", getEntityName()));
            getSession().update(t);
            log.info("Successful updated.");

        } catch (HibernateException e) {
            log.error("Error:", e);
        }
        return t;
    }

    @Transactional(readOnly = true)
    @Override
    public Set<T> loadAll() {
        Set<T> set = new LinkedHashSet<>();
        try {
            log.info(format("Loading all %ss", getEntityName()));
            set.addAll(getSession().createCriteria(getEntityClass()).list());
            log.info("Successful loaded. Size: " + set.size());

        } catch (HibernateException e) {
            log.error("Error:", e);
        }
        return set;
    }

    Class<T> getEntityClass() {
        return clazz;
    }

    void setEntityClass(Class<T> clazz) {
        this.clazz = clazz;
    }

    private String getEntityName() {
        return getEntityClass().getSimpleName();
    }
}