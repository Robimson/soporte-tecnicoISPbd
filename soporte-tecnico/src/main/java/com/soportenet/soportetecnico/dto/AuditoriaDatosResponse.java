package com.soportenet.soportetecnico.dto;

import java.time.OffsetDateTime;

public class AuditoriaDatosResponse {

    private Long idAuditoria;
    private String tablaAfectada;
    private String operacion;

    private Long idUsuarioResponsable;
    private String nombreUsuario;
    private String correo;
    private String rol;
    private String estadoCuenta;

    private String datosAnteriores;
    private String datosNuevos;

    private OffsetDateTime fecha;

    public AuditoriaDatosResponse() {
    }

    public AuditoriaDatosResponse(
            Long idAuditoria,
            String tablaAfectada,
            String operacion,
            Long idUsuarioResponsable,
            String nombreUsuario,
            String correo,
            String rol,
            String estadoCuenta,
            String datosAnteriores,
            String datosNuevos,
            OffsetDateTime fecha
    ) {
        this.idAuditoria = idAuditoria;
        this.tablaAfectada = tablaAfectada;
        this.operacion = operacion;
        this.idUsuarioResponsable = idUsuarioResponsable;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.rol = rol;
        this.estadoCuenta = estadoCuenta;
        this.datosAnteriores = datosAnteriores;
        this.datosNuevos = datosNuevos;
        this.fecha = fecha;
    }

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
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

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }
}