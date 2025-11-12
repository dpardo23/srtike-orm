package com.dpardo.strike.repository;

import com.dpardo.strike.domain.EquipoComboItem;
import com.dpardo.strike.entity.Equipo;
import com.dpardo.strike.entity.Pais;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para la entidad Equipo.
 * Migrado 100% a Hibernate.
 */
public class EquipoRepository {

    // Este método a veces se llama desde el controlador de Equipos para llenar el combo de países
    public List<String> obtenerCodigosPaises() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("SELECT p.codFifa FROM Pais p ORDER BY p.codFifa", String.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<EquipoComboItem> obtenerEquiposParaCombo() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("SELECT new com.dpardo.strike.domain.EquipoComboItem(e.idEquipo, e.nombre) FROM Equipo e ORDER BY e.nombre", EquipoComboItem.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void insertarEquipo(int id, String nombre, String paisCodFifa, String ciudad, LocalDate fechaFundacion, String dt) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            // Buscar la entidad foránea
            Pais paisRef = em.find(Pais.class, paisCodFifa);
            if (paisRef == null) {
                throw new Exception("El país con código " + paisCodFifa + " no existe.");
            }

            Equipo equipo = new Equipo();
            equipo.setIdEquipo(id);
            equipo.setNombre(nombre);
            equipo.setPais(paisRef); // Asignar objeto relación
            equipo.setCiudad(ciudad);
            equipo.setfFundacion(fechaFundacion);
            equipo.setDirectorTecnico(dt);

            em.persist(equipo);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al insertar equipo: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}