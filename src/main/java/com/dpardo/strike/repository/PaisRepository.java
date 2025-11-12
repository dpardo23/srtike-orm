package com.dpardo.strike.repository;

import com.dpardo.strike.domain.PaisComboItem;
import com.dpardo.strike.entity.Pais;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para la entidad Pais.
 * Migrado 100% a Hibernate.
 */
public class PaisRepository {

    public List<Pais> obtenerTodosLosPaises() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            // Usamos JPQL para traer todos los objetos Pais
            return em.createQuery("SELECT p FROM Pais p ORDER BY p.nombre", Pais.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<PaisComboItem> obtenerPaisesParaCombo() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            // Proyección directa al record DTO usando constructor expression
            String jpql = "SELECT new com.dpardo.strike.domain.PaisComboItem(p.codFifa, p.nombre) " +
                    "FROM Pais p ORDER BY p.nombre";
            return em.createQuery(jpql, PaisComboItem.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void insertarPais(String codFifa, String nombre, String continente) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            Pais pais = new Pais();
            pais.setCodFifa(codFifa);
            pais.setNombre(nombre);
            pais.setContinente(continente);

            em.persist(pais);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al insertar país: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<String> obtenerTodosLosCodigosFifa() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("SELECT p.codFifa FROM Pais p ORDER BY p.codFifa", String.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void actualizarNombrePais(String codFifa, String nuevoNombre) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            Pais pais = em.find(Pais.class, codFifa);
            if (pais == null) {
                throw new Exception("No se encontró el país con código: " + codFifa);
            }
            pais.setNombre(nuevoNombre); // Hibernate detecta el cambio y hace UPDATE al commit

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al actualizar país: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void eliminarPais(String codFifa) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            Pais pais = em.find(Pais.class, codFifa);
            if (pais != null) {
                em.remove(pais);
            } else {
                throw new Exception("No existe el país para eliminar: " + codFifa);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al eliminar país (posible restricción de FK): " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}