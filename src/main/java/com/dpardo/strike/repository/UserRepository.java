package com.dpardo.strike.repository;

import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.entity.Session;
import com.dpardo.strike.entity.User;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Repositorio de Usuario.
 * 100% Migrado a JPA/Hibernate. Sin procedimientos almacenados.
 */
public class UserRepository {

    /**
     * Autentica al usuario usando JPQL y Java, registra la sesión y devuelve la info.
     */
    public SessionInfo authenticateAndRegisterSession(String username, String password) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            // 1. Buscar Usuario y su Rol Activo
            // Hacemos un JOIN explícito porque las relaciones en User.java estaban comentadas
            String jpql = "SELECT u, r.nombre " +
                    "FROM User u " +
                    "JOIN UserRol ur ON u.idUser = ur.id.idUser " +
                    "JOIN Rol r ON ur.id.idRol = r.idRol " +
                    "WHERE u.nombreUsuario = :username " +
                    "AND ur.activo = true";

            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("username", username);

            Object[] result;
            try {
                result = query.getSingleResult();
            } catch (NoResultException e) {
                System.err.println("Usuario no encontrado o sin rol activo: " + username);
                return null;
            }

            User user = (User) result[0];
            String roleName = (String) result[1];

            // 2. Verificar Contraseña (En texto plano según tu esquema actual)
            if (!user.getContrasena().equals(password)) {
                System.err.println("Contraseña incorrecta para: " + username);
                return null;
            }

            // 3. Registrar la Sesión
            tx = em.getTransaction();
            tx.begin();

            // IMPORTANTE: Si quisieras auditar el LOGIN, aquí llamarías a HibernateUtil.setAuditUser(em);
            // Pero como el usuario apenas se está logueando, aún no está en SessionManager.
            // Para el login inicial, los triggers pueden recibir NULL o lo manejamos después.

            // Datos simulados del cliente (ya que es Desktop App local)
            String clientIp = "127.0.0.1";
            int clientPort = (int) (Math.random() * 60000) + 1024;
            // Obtenemos el PID real de la JVM
            int pid = (int) ProcessHandle.current().pid();

            Session newSession = new Session(clientIp, clientPort, pid, user);
            em.persist(newSession);

            tx.commit();

            // 4. Retornar DTO con la info
            // Nota: newSession.getIdSession() ya tiene el valor generado por la BD gracias a @Identity
            return new SessionInfo(
                    user.getIdUser(),
                    pid,
                    clientIp,
                    clientPort,
                    roleName
            );

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
}