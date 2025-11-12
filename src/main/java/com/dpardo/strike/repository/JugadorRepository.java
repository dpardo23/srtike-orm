package com.dpardo.strike.repository;

import com.dpardo.strike.entity.Equipo;
import com.dpardo.strike.entity.Jugador;
import com.dpardo.strike.entity.Pais;
import com.dpardo.strike.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para la entidad Jugador.
 * Migrado 100% a Hibernate.
 */
public class JugadorRepository {

    public void insertarJugador(int id, String nombre, LocalDate fechaNacimiento, char sexo,
                                String paisCodFifa, String posicion, int equipoId,
                                Integer estadisticas, int altura, BigDecimal peso, byte[] foto) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            // Validar relaciones
            Pais paisRef = em.find(Pais.class, paisCodFifa);
            Equipo equipoRef = em.find(Equipo.class, equipoId);

            if (paisRef == null) throw new Exception("País no encontrado: " + paisCodFifa);
            if (equipoRef == null) throw new Exception("Equipo no encontrado: " + equipoId);

            Jugador j = new Jugador();
            j.setIdJugador(id);
            j.setNombre(nombre);
            j.setfNacimiento(fechaNacimiento);
            j.setSexo(sexo);
            j.setPais(paisRef);
            j.setPosicion(posicion);
            j.setEquipo(equipoRef);
            j.setEstadisticas(estadisticas);
            j.setAltura(altura);
            j.setPeso(peso);
            j.setFoto(foto);

            em.persist(j);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al insertar jugador: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<String> obtenerTodosLosNombresDeJugadores() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("SELECT j.nombre FROM Jugador j ORDER BY j.nombre", String.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void eliminarJugadorPorNombre(String nombre) throws Exception {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            HibernateUtil.setAuditUser(em); // <--- Auditoría

            // Usamos JPQL Bulk Delete
            int eliminados = em.createQuery("DELETE FROM Jugador j WHERE j.nombre = :nombre")
                    .setParameter("nombre", nombre)
                    .executeUpdate();

            if (eliminados == 0) {
                throw new Exception("No se encontró ningún jugador con nombre: " + nombre);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new Exception("Error al eliminar jugador: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}