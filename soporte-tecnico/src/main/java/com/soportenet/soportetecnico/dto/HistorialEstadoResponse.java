package com.soportenet.soportetecnico.dto;

import java.time.OffsetDateTime;

public class HistorialEstadoResponse {

    private Long idHistorial;
    private Long idSolicitud;

    private Integer estadoAnterior;
    private String nombreEstadoAnterior;

    private Integer estadoNuevo;
    private String nombreEstadoNuevo;

    private OffsetDateTime fechaCambio;

    private Long idUsuarioResponsable;
    private String nombreUsuario;
    private String correo;
    private String rol;

    public HistorialEstadoResponse() {
    }

    public HistorialEstadoResponse(
            Long idHistorial,
            Long idSolicitud,
            Integer estadoAnterior,
            String nombreEstadoAnterior,
            Integer estadoNuevo,
            String nombreEstadoNuevo,
            OffsetDateTime fechaCambio,
            Long idUsuarioResponsable,
            String nombreUsuario,
            String correo,
            String rol
    ) {
        this.idHistorial = idHistorial;
        this.idSolicitud = idSolicitud;
        this.estadoAnterior = estadoAnterior;
        this.nombreEstadoAnterior = nombreEstadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.nombreEstadoNuevo = nombreEstadoNuevo;
        this.fechaCambio = fechaCambio;
        this.idUsuarioResponsable = idUsuarioResponsable;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.rol = rol;
    }

    public Long getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(Integer estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getNombreEstadoAnterior() {
        return nombreEstadoAnterior;
    }

    public void setNombreEstadoAnterior(String nombreEstadoAnterior) {
        this.nombreEstadoAnterior = nombreEstadoAnterior;
    }

    public Integer getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(Integer estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getNombreEstadoNuevo() {
        return nombreEstadoNuevo;
    }

    public void setNombreEstadoNuevo(String nombreEstadoNuevo) {
        this.nombreEstadoNuevo = nombreEstadoNuevo;
    }

    public OffsetDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(OffsetDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public Long getIdUsuarioResponsable() {
        return idUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Long idUsuarioResponsable) {
        this.idUsuarioResponsable = idUsuarioResponsable;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}