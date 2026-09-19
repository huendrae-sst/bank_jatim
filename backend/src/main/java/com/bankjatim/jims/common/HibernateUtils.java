package com.bankjatim.jims.common;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public class HibernateUtils {

    public static Long getId(Object entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof HibernateProxy proxy) {
            try {
                Object id = proxy.getHibernateLazyInitializer().getIdentifier();
                if (id instanceof Long l) return l;
                if (id instanceof Number n) return n.longValue();
                if (id != null) return Long.valueOf(id.toString());
            } catch (Exception ignored) {
            }
        }
        if (entity instanceof BaseEntity baseEntity) {
            try {
                return baseEntity.getId();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static boolean isInitialized(Object entity) {
        if (entity == null) {
            return false;
        }
        try {
            return Hibernate.isInitialized(entity);
        } catch (Exception e) {
            return false;
        }
    }
}
