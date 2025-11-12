package com.dpardo.strike.repository;

import com.dpardo.strike.domain.SessionViewModel;
import com.dpardo.strike.domain.UiComboItem;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para SuperAdmin.
 * Reemplaza funciones almacenadas con SQL Nativo gestionado por Hibernate.
 */
public class SuperAdminRepository {

    @SuppressWarnings("unchecked")
    public List<SessionViewModel> obtenerSesionesActivas() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            // Replicamos la lógica de "obtener_sesiones_activas_detalladas" con SQL nativo
            // para no depender de la función en la BD.
            String sql =
                    "SELECT s.pid, u.nombre_usuario, u.email, u.fecha_creacion, " +
                            "       r.nombre AS nombre_rol, ui.cod_componente AS cod_componente_ui, " +
                            "       s.user_addr, s.user_port, ur.fecha_asignacion, ur.activo " +
                            "FROM session s " +
                            "JOIN \"user\" u ON s.id_user = u.id_user " +
                            "JOIN user_rol ur ON u.id_user = ur.id_user " +
                            "JOIN rol r ON ur.id_rol = r.id_rol " +
                            "JOIN rol_permission rp ON r.id_rol = rp.id_rol " +
                            "JOIN permission per ON rp.id_permission = per.id_permission " +
                            "JOIN permission_ui pu ON per.id_permission = pu.id_permission " +
                            "JOIN ui ON pu.id_ui = ui.id_ui " +
                            "WHERE ur.activo = true " +
                            "  AND s.pid IS NOT NULL " +
                            // Filtramos para obtener solo la UI principal, asumiendo alguna lógica
                            // o simplemente trayendo todas. Para evitar duplicados exactos del procedure,
                            // usamos DISTINCT si es necesario.
                            "ORDER BY s.pid DESC";

            Query query = em.createNativeQuery(sql);
            List<Object[]> results = query.getResultList();

            List<SessionViewModel> lista = new ArrayList<>();
            for (Object[] row : results) {
                // Casteo manual seguro de los resultados Object[]
                Integer pid = ((Number) row[0]).intValue();
                String nombreUser = (String) row[1];
                String email = (String) row[2];
                Timestamp fecCreacion = (row[3] != null) ? Timestamp.valueOf(((java.sql.Timestamp) row[3]).toLocalDateTime()) : null;
                String nombreRol = (String) row[4];
                String codUi = (String) row[5];
                String addr = (String) row[6]; // inet suele venir como string en JDBC driver moderno, o PGobject
                Integer port = ((Number) row[7]).intValue();
                Timestamp fecAsig = (row[8] != null) ? Timestamp.valueOf(((java.sql.Timestamp) row[8]).toLocalDateTime()) : null;
                Boolean activo = (Boolean) row[9];

                lista.add(new SessionViewModel(
                        pid, nombreUser, email, fecCreacion, nombreRol, codUi,
                        addr != null ? addr.toString() : "",
                        port, fecAsig, activo
                ));
            }
            return lista;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    public List<UiComboItem> obtenerUis(int userId) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            // Lógica SQL equivalente a "obtener_uis_por_usuario"
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
                            "AND pu.activo = true";

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