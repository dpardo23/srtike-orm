package com.dpardo.strike.repository;

import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.domain.UiComboItem;
import com.dpardo.strike.entity.Session;
import com.dpardo.strike.entity.User;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de Usuario.
 * Encargado de la autenticación y de los permisos/navegación del usuario.
 */
public class UserRepository {

    /**
     * Autentica al usuario y registra la sesión.
     */
    public SessionInfo authenticateAndRegisterSession(String username, String password) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            // 1. Buscar Usuario y su Rol Activo
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

            // 2. Verificar Contraseña
            if (!user.getContrasena().equals(password)) {
                System.err.println("Contraseña incorrecta para: " + username);
                return null;
            }

            // 3. Registrar la Sesión
            tx = em.getTransaction();
            tx.begin();

            // Datos simulados del cliente
            String clientIp = "127.0.0.1";
            int clientPort = (int) (Math.random() * 60000) + 1024;
            int pid = (int) ProcessHandle.current().pid();

            Session newSession = new Session(clientIp, clientPort, pid, user);
            em.persist(newSession);

            tx.commit();

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
     * (Reemplaza la lógica antigua de sub-consultas con Joins eficientes)
     */
    @SuppressWarnings("unchecked")
    public List<UiComboItem> obtenerUisPermitidas(int userId) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            // Esta consulta replica tu lógica de navegación original:
            // Partimos de las tablas intermedias hasta llegar a la UI.
            String sql =
                    "SELECT DISTINCT ui.id_ui, ui.cod_componente, ui.descripcion " +
                            "FROM ui " +
                            "JOIN permission_ui pu ON ui.id_ui = pu.id_ui " +         // UI <-> PermisoUI
                            "JOIN permission p ON pu.id_permission = p.id_permission " +
                            "JOIN rol_permission rp ON p.id_permission = rp.id_permission " + // Permiso <-> RolPermiso
                            "JOIN user_rol ur ON rp.id_rol = ur.id_rol " +            // RolPermiso <-> UsuarioRol
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