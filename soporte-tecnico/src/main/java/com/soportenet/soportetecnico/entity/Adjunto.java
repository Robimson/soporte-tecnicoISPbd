package com.soportenet.soportetecnico.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "adjunto")
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adjunto")
    private Long idAdjunto;

    @Column(name = "id_solicitud", nullable = false)
    private Long idSolicitud;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tipo_archivo", nullable = false, length = 50)
    private String tipoArchivo;

    @Column(name = "tamano_archivo", nullable = false)
    private Long tamanoArchivo;

    @Column(name = "url_almacenamiento", nullable = false, length = 500)
    private String urlAlmacenamiento;

    @Column(name = "fecha_subida", nullable = false)
    private OffsetDateTime fechaSubida;

    @Column(name = "id_usuario_sube")
    private Long idUsuarioSube;

    public Adjunto() {
    }

    public Long getIdAdjunto() {
        return idAdjunto;
    }

    public void setIdAdjunto(Long idAdjunto) {
        this.idAdjunto = idAdjunto;
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public Long getTamanoArchivo() {
        return tamanoArchivo;
    }

    public void setTamanoArchivo(Long tamanoArchivo) {
        this.tamanoArchivo = tamanoArchivo;
    }

    public String getUrlAlmacenamiento() {
        return urlAlmacenamiento;
    }

    public void setUrlAlmacenamiento(String urlAlmacenamiento) {
        this.urlAlmacenamiento = urlAlmacenamiento;
    }

    public OffsetDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(OffsetDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public Long getIdUsuarioSube() {
        return idUsuarioSube;
    }

    public void setIdUsuarioSube(Long idUsuarioSube) {
        this.idUsuarioSube = idUsuarioSube;
    }
}