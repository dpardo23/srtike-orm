package com.dpardo.strike.repository;

import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.domain.UiComboItem;
import com.dpardo.strike.entity.Session;
import com.dpardo.strike.entity.User;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException; // Sigue siendo útil
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List; // Importante

/**
 * Repositorio de Usuario.
 * CORREGIDO: Maneja usuarios con múltiples roles.
 */
public class UserRepository {

    /**
     * Autentica al usuario. Si tiene múltiples roles, inicia sesión con el
     * rol de ID más alto (más privilegiado).
     */
    public SessionInfo authenticateAndRegisterSession(String username, String password) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            // 1. Buscar Usuario y sus Roles Activos, ordenados por privilegio (ID)
            String jpql = "SELECT u, r.nombre " +
                    "FROM User u " +
                    "JOIN UserRol ur ON u.idUser = ur.id.idUser " +
                    "JOIN Rol r ON ur.id.idRol = r.idRol " +
                    "WHERE u.nombreUsuario = :username " +
                    "AND ur.activo = true " +
                    "ORDER BY r.idRol DESC"; // <-- CORRECCIÓN: Ordenamos por ID de Rol (3, 2, 1)

            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setParameter("username", username);

            // --- INICIO DE LA CORRECCIÓN DE LÓGICA ---

            // Usamos getResultList() en lugar de getSingleResult()
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                // Si la lista está vacía, el usuario no existe o no tiene roles activos
                System.err.println("Usuario no encontrado o sin rol activo: " + username);
                return null;
            }

            // Tomamos el primer resultado (que es el rol más alto gracias al ORDER BY)
            Object[] result = results.get(0);

            // --- FIN DE LA CORRECCIÓN DE LÓGICA ---

            User user = (User) result[0];
            String roleName = (String) result[1];

            // 2. Verificar Contraseña
            if (!user.getContrasena().equals(password)) {
                System.err.println("Contraseña incorrecta para: " + username);
                return null;
            }

            // 3. Registrar la Sesión
            tx = em.getTransaction();
            tx.begin();

            String clientIp = "127.0.0.1";
            int clientPort = (int) (Math.random() * 60000) + 1024;
            int pid = (int) ProcessHandle.current().pid();

            Session newSession = new Session(clientIp, clientPort, pid, user);
            em.persist(newSession);

            tx.commit();

            // Devolvemos la sesión con el rol de mayor privilegio
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

    /**
     * Obtiene las UIs permitidas para un usuario siguiendo la ruta:
     * User -> UserRol -> RolPermission -> PermissionUi -> UI
     * (Este método ya funciona correctamente con múltiples roles)
     */
    @SuppressWarnings("unchecked")
    public List<UiComboItem> obtenerUisPermitidas(int userId) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            String sql =
                    "SELECT DISTINCT ui.id_ui, ui.cod_componente, ui.descripcion " +
                            "FROM ui " +
                            "JOIN permission_ui pu ON ui.id_ui = pu.id_ui " +
                            "JOIN permission p ON pu.id_permission = p.id_permission " +
                            "JOIN rol_permission rp ON p.id_permission = rp.id_permission " +
                            "JOIN user_rol ur ON rp.id_rol = ur.id_rol " +
                            "WHERE ur.id_user = ? " +
                            "AND ur.activo = true " +
                            "AND rp.activo = true " +
                            "AND pu.activo = true " +
                            "ORDER BY ui.id_ui";

            Query query = em.createNativeQuery(sql);
            query.setParameter(1, userId);

            List<Object[]> results = query.getResultList();
            List<UiComboItem> lista = new ArrayList<>();

            for (Object[] row : results) {
                lista.add(new UiComboItem(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        (String) row[2]
                ));
            }
            return lista;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}