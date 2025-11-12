package com.dpardo.strike.util;

import com.dpardo.strike.domain.SessionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session; // Importante: Session de Hibernate, no tu entidad

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utilidad Singleton para Hibernate.
 * Incluye soporte para Auditoría (Triggers de PostgreSQL).
 */
public class HibernateUtil {

    private static final EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("strike-pu");
        } catch (Throwable ex) {
            System.err.println("Error inicializando EntityManagerFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Configura la variable de sesión 'app.current_user_id' en la base de datos
     * para que los Triggers de auditoría funcionen correctamente.
     * Debe llamarse justo después de em.getTransaction().begin().
     */
    public static void setAuditUser(EntityManager em) {
        Integer currentUserId = SessionManager.getCurrentUserId();

        // Solo ejecutamos esto si hay un usuario logueado
        if (currentUserId != null) {
            // Desempaquetamos la sesión de Hibernate para acceder a la conexión JDBC nativa
            // Esto es más seguro y rápido que crear queries nativas JPA para variables de sesión
            try {
                em.unwrap(Session.class).doWork(connection -> {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SET app.current_user_id = " + currentUserId);
                    }
                });
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo establecer el usuario de auditoría. " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void shutdown() {
        if (emf != null) {
            emf.close();
        }
    }
}