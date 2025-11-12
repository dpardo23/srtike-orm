package com.dpardo.strike.repository;

import com.dpardo.strike.domain.SessionViewModel;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SuperAdminRepository {

    @SuppressWarnings("unchecked")
    public List<SessionViewModel> obtenerSesionesActivas() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            // Consulta para ver quién está conectado
            String sql =
                    "SELECT s.pid, u.nombre_usuario, u.email, u.fecha_creacion, " +
                            "       r.nombre AS nombre_rol, ui.cod_componente AS cod_componente_ui, " +
                            "       s.user_addr, s.user_port, ur.fecha_asignacion, ur.activo " +
                            "FROM session s " +
                            "JOIN \"user\" u ON s.id_user = u.id_user " +
                            "JOIN user_rol ur ON u.id_user = ur.id_user " +
                            "JOIN rol r ON ur.id_rol = r.id_rol " +
                            "LEFT JOIN rol_permission rp ON r.id_rol = rp.id_rol " + // LEFT JOIN por si no tienen permisos
                            "LEFT JOIN permission per ON rp.id_permission = per.id_permission " +
                            "LEFT JOIN permission_ui pu ON per.id_permission = pu.id_permission " +
                            "LEFT JOIN ui ON pu.id_ui = ui.id_ui " +
                            "WHERE ur.activo = true " +
                            "  AND s.pid IS NOT NULL " +
                            "ORDER BY s.pid DESC";

            Query query = em.createNativeQuery(sql);
            List<Object[]> results = query.getResultList();

            // ... (Mapeo igual que antes, sin cambios en la lógica de mapeo) ...
            List<SessionViewModel> lista = new ArrayList<>();
            for (Object[] row : results) {
                Integer pid = ((Number) row[0]).intValue();
                // ... (resto del mapeo seguro) ...
                // Para simplificar el copy-paste, asumo que mantienes el mapeo que te di antes
                // Si lo necesitas completo de nuevo avísame, pero es el mismo bloque "for" de la respuesta anterior.

                // (Agrego el mapeo rápido para que el archivo sea funcional al copiar)
                String nombreUser = (String) row[1];
                String email = (String) row[2];
                Timestamp fecCreacion = (row[3] != null) ? Timestamp.valueOf(((java.sql.Timestamp) row[3]).toLocalDateTime()) : null;
                String nombreRol = (String) row[4];
                String codUi = (row[5] != null) ? (String) row[5] : "N/A";
                String addr = (String) row[6];
                Integer port = ((Number) row[7]).intValue();
                Timestamp fecAsig = (row[8] != null) ? Timestamp.valueOf(((java.sql.Timestamp) row[8]).toLocalDateTime()) : null;
                Boolean activo = (Boolean) row[9];

                lista.add(new SessionViewModel(pid, nombreUser, email, fecCreacion, nombreRol, codUi, addr, port, fecAsig, activo));
            }
            return lista;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ¡MÉTODO obtenerUis ELIMINADO DE AQUÍ!
}