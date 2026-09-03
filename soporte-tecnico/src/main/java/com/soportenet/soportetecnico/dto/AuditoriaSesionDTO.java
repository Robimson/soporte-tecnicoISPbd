package com.soportenet.soportetecnico.dto;

import java.time.OffsetDateTime;

public class AuditoriaSesionDTO {

    private Long idSesion;
    private Long idUsuario;
    private String nombreUsuario;
    private String correo;
    private String rol;
    private String ipOrigen;
    private OffsetDateTime fechaEntrada;
    private OffsetDateTime ultimaActividad;
    private OffsetDateTime fechaSalida;
    private boolean activa;

    public Long getIdSesion() { return idSesion; }
    public void setIdSesion(Long idSesion) { this.idSesion = idSesion; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    public OffsetDateTime getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(OffsetDateTime fechaEntrada) { this.fechaEntrada = fechaEntrada; }

    public OffsetDateTime getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(OffsetDateTime ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public OffsetDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(OffsetDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}