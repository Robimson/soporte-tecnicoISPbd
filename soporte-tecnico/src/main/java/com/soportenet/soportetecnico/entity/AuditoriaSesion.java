package com.soportenet.soportetecnico.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auditoria_sesion")
public class AuditoriaSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Long idSesion;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "fecha_entrada", nullable = false)
    private OffsetDateTime fechaEntrada;

    @Column(name = "ultima_actividad", nullable = false)
    private OffsetDateTime ultimaActividad;

    @Column(name = "fecha_salida")
    private OffsetDateTime fechaSalida;

    /*
     * PostgreSQL usa INET.
     * Se recibe como String para evitar acoplar la entidad
     * a tipos específicos de PostgreSQL.
     */
    @Column(name = "ip_origen", columnDefinition = "inet")
    private String ipOrigen;

    public AuditoriaSesion() {
    }

    public Long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Long idSesion) {
        this.idSesion = idSesion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public OffsetDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(OffsetDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public OffsetDateTime getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(OffsetDateTime ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public OffsetDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(OffsetDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }
}