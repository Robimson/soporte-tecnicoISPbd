package com.soportenet.soportetecnico.service;

import com.soportenet.soportetecnico.repository.AuditoriaSesionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar la auditoría de sesiones.
 *
 * No modifica la lógica de autenticación existente.
 * Se encarga únicamente de registrar:
 * - apertura de sesión
 * - cierre de sesión
 * - actualización de última actividad
 */
@Service
public class AuditoriaSesionService {

    private final AuditoriaSesionRepository auditoriaSesionRepository;

    public AuditoriaSesionService(
            AuditoriaSesionRepository auditoriaSesionRepository) {

        this.auditoriaSesionRepository = auditoriaSesionRepository;
    }

    /**
     * Registra una nueva sesión.
     *
     * @param idUsuario usuario que inicia sesión
     * @param ipOrigen IP desde donde inicia sesión
     * @return id de la sesión creada
     */
    @Transactional
    public Long abrirSesion(
            Long idUsuario,
            String ipOrigen) {

        return auditoriaSesionRepository.abrirSesion(
                idUsuario,
                ipOrigen
        );
    }

    /**
     * Cierra una sesión activa.
     *
     * @param idSesion identificador de la sesión
     */
    @Transactional
    public void cerrarSesion(Long idSesion) {

        auditoriaSesionRepository.cerrarSesion(
                idSesion
        );
    }

    /**
     * Actualiza la fecha/hora de última actividad.
     *
     * @param idSesion identificador de la sesión
     */
    @Transactional
    public void tocarActividad(Long idSesion) {

        auditoriaSesionRepository.tocarActividad(
                idSesion
        );
    }
}