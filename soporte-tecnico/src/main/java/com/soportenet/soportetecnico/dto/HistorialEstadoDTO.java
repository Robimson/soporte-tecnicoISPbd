package com.soportenet.soportetecnico.dto;

import java.time.OffsetDateTime;

public class HistorialEstadoDTO {

    private Long idHistorial;
    private Long idSolicitud;
    private String estadoAnterior;
    private String estadoNuevo;
    private OffsetDateTime fechaCambio;
    private String nombreUsuarioResponsable;
    private String rolResponsable;

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }

    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public OffsetDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(OffsetDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public String getNombreUsuarioResponsable() { return nombreUsuarioResponsable; }
    public void setNombreUsuarioResponsable(String nombreUsuarioResponsable) { this.nombreUsuarioResponsable = nombreUsuarioResponsable; }

    public String getRolResponsable() { return rolResponsable; }
    public void setRolResponsable(String rolResponsable) { this.rolResponsable = rolResponsable; }
}