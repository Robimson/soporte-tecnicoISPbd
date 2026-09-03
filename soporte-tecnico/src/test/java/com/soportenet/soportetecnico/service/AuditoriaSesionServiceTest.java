package com.soportenet.soportetecnico.service;

import com.soportenet.soportetecnico.repository.AuditoriaSesionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuditoriaSesionServiceTest {

    private AuditoriaSesionRepository auditoriaSesionRepository;
    private AuditoriaSesionService auditoriaSesionService;

    @BeforeEach
    void configurar() {

        auditoriaSesionRepository =
                Mockito.mock(AuditoriaSesionRepository.class);

        auditoriaSesionService =
                new AuditoriaSesionService(
                        auditoriaSesionRepository
                );
    }

    @Test
    void abrirSesionDevuelveIdSesion() {

        Long idUsuario = 10L;
        String ipOrigen = "127.0.0.1";

        when(
                auditoriaSesionRepository.abrirSesion(
                        idUsuario,
                        ipOrigen
                )
        ).thenReturn(100L);

        Long idSesion =
                auditoriaSesionService.abrirSesion(
                        idUsuario,
                        ipOrigen
                );

        assertEquals(100L, idSesion);

        verify(auditoriaSesionRepository)
                .abrirSesion(idUsuario, ipOrigen);
    }

    @Test
    void cerrarSesionInvocaRepositorio() {

        Long idSesion = 100L;

        auditoriaSesionService.cerrarSesion(idSesion);

        verify(auditoriaSesionRepository)
                .cerrarSesion(idSesion);
    }

    @Test
    void tocarActividadInvocaRepositorio() {

        Long idSesion = 100L;

        auditoriaSesionService.tocarActividad(idSesion);

        verify(auditoriaSesionRepository)
                .tocarActividad(idSesion);
    }
}