package com.soportenet.soportetecnico.dto;

import com.soportenet.soportetecnico.enums.OperacionAuditoria;

import java.time.OffsetDateTime;
import java.util.Map;

public class AuditoriaDatosDTO {

    private Long idAuditoria;
    private OffsetDateTime fecha;
    private String tablaAfectada;
    private OperacionAuditoria operacion;
    private Map<String, Object> datosAnteriores;
    private Map<String, Object> datosNuevos;
    private Long idUsuarioResponsable;
    private String nombreUsuario;
    private String correo;
    private String rol;

    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }

    public OffsetDateTime getFecha() { return fecha; }
    public void setFecha(OffsetDateTime fecha) { this.fecha = fecha; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public OperacionAuditoria getOperacion() { return operacion; }
    public void setOperacion(OperacionAuditoria operacion) { this.operacion = operacion; }

    public Map<String, Object> getDatosAnteriores() { return datosAnteriores; }
    public void setDatosAnteriores(Map<String, Object> datosAnteriores) { this.datosAnteriores = datosAnteriores; }

    public Map<String, Object> getDatosNuevos() { return datosNuevos; }
    public void setDatosNuevos(Map<String, Object> datosNuevos) { this.datosNuevos = datosNuevos; }

    public Long getIdUsuarioResponsable() { return idUsuarioResponsable; }
    public void setIdUsuarioResponsable(Long idUsuarioResponsable) { this.idUsuarioResponsable = idUsuarioResponsable; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}