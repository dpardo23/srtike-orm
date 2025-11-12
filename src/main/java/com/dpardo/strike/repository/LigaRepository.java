package com.dpardo.strike.repository;

import com.dpardo.strike.domain.LigaComboItem;
import com.dpardo.strike.entity.Liga;
import com.dpardo.strike.entity.Pais;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para la entidad Liga.
 * Migrado 100% a Hibernate.
 */
public class LigaRepository {

    public List<LigaComboItem> obtenerLigasParaCombo() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("SELECT new com.dpardo.strike.domain.LigaComboItem(l.idLiga, l.nombre) FROM Liga l ORDER BY l.nombre", LigaComboItem.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void insertarLiga(int id, String nombre, String paisCodFifa, String tipo) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            Pais paisRef = em.find(Pais.class, paisCodFifa);
            if (paisRef == null) throw new Exception("País no encontrado: " + paisCodFifa);

            Liga liga = new Liga();
            liga.setIdLiga(id);
            liga.setNombre(nombre);
            liga.setPais(paisRef);
            liga.setTipo(tipo);

            em.persist(liga);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al insertar liga: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}