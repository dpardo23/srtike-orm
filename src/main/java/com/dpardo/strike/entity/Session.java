package com.dpardo.strike.entity;

import jakarta.persistence.*;

/**
 * Clase Entidad que mapea la tabla 'session'.
 * CORREGIDO: Se usa id_session como Clave Primaria única (IDENTITY).
 */
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_session", nullable = false)
    private Integer idSession;

    @Column(name = "user_addr", nullable = false)
    private String userAddr; // 'inet' se mapea a String

    @Column(name = "user_port", nullable = false)
    private Integer userPort;

    @Column(name = "pid")
    private Integer pid;

    // Relación con User
    // Nota: En la BD es parte de la PK compuesta, pero en JPA la tratamos como
    // una relación obligatoria. Hibernate manejará la FK 'id_user'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    public Session() {
    }

    public Session(String userAddr, Integer userPort, Integer pid, User user) {
        this.userAddr = userAddr;
        this.userPort = userPort;
        this.pid = pid;
        this.user = user;
    }

    // --- Getters y Setters ---

    public Integer getIdSession() {
        return idSession;
    }

    public void setIdSession(Integer idSession) {
        this.idSession = idSession;
    }

    public String getUserAddr() {
        return userAddr;
    }

    public void setUserAddr(String userAddr) {
        this.userAddr = userAddr;
    }

    public Integer getUserPort() {
        return userPort;
    }

    public void setUserPort(Integer userPort) {
        this.userPort = userPort;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}