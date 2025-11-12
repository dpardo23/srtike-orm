package com.dpardo.strike.repository;

import com.dpardo.strike.entity.Equipo;
import com.dpardo.strike.entity.Liga;
import com.dpardo.strike.entity.Partido;
import com.dpardo.strike.entity.PartidoId;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Repositorio para la entidad Partido.
 * Maneja Clave Primaria Compuesta (@EmbeddedId).
 */
public class PartidoRepository {

    public void insertarPartido(int equipoLocalId, int equipoVisitanteId, LocalDate fecha, LocalTime hora, int ligaId, int historial) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            // Buscar referencias
            Equipo local = em.find(Equipo.class, equipoLocalId);
            Equipo visita = em.find(Equipo.class, equipoVisitanteId);
            Liga liga = em.find(Liga.class, ligaId);

            if (local == null) throw new Exception("Equipo local no existe: " + equipoLocalId);
            if (visita == null) throw new Exception("Equipo visitante no existe: " + equipoVisitanteId);
            if (liga == null) throw new Exception("Liga no existe: " + ligaId);

            // Crear ID Compuesta
            PartidoId id = new PartidoId(equipoLocalId, equipoVisitanteId, fecha);

            // Crear Entidad
            Partido p = new Partido();
            p.setId(id);
            p.setHora(hora);
            p.setHistorial(historial);
            // Asignar relaciones (Importante para @MapsId si se usa, o para consistencia)
            p.setEquipoLocal(local);
            p.setEquipoVisitante(visita);
            p.setLiga(liga);

            em.persist(p);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al insertar partido: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}