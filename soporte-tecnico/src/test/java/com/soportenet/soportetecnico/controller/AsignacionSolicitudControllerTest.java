package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.AsignarSolicitudRequest;
import com.soportenet.soportetecnico.entity.Solicitud;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AsignacionSolicitudControllerTest {

    private SolicitudRepository solicitudRepository;
    private AsignacionSolicitudController controller;

    @BeforeEach
    void configurar() {
        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        controller = new AsignacionSolicitudController(solicitudRepository);
    }

    @Test
    void administradorPuedeAsignarSolicitudATecnico() {

        Long idSolicitud = 100L;
        Long idAdministrador = 1L;
        Long idTecnico = 5L;

        AsignarSolicitudRequest request = new AsignarSolicitudRequest();
        request.setIdTecnico(idTecnico);
        request.setIdGrupo(null);
        request.setIdPrioridad(1);
        request.setMotivoReasignacion(null);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idAdministrador.toString(),
                        null
                );

        ResponseEntity<?> respuesta =
                controller.asignar(
                        idSolicitud,
                        request,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        verify(solicitudRepository).asignarSolicitud(
                idSolicitud,
                idAdministrador,
                idTecnico,
                null,
                1,
                null
        );
    }

    @Test
    void administradorPuedeAsignarSolicitudAGrupo() {

        Long idSolicitud = 101L;
        Long idAdministrador = 1L;
        Long idGrupo = 20L;

        AsignarSolicitudRequest request = new AsignarSolicitudRequest();
        request.setIdTecnico(null);
        request.setIdGrupo(idGrupo);
        request.setIdPrioridad(2);
        request.setMotivoReasignacion(null);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idAdministrador.toString(),
                        null
                );

        ResponseEntity<?> respuesta =
                controller.asignar(
                        idSolicitud,
                        request,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        verify(solicitudRepository).asignarSolicitud(
                idSolicitud,
                idAdministrador,
                null,
                idGrupo,
                2,
                null
        );
    }

    @Test
    void reasignacionEnviaMotivoAlProcedimiento() {

        Long idSolicitud = 102L;
        Long idAdministrador = 1L;
        Long idTecnico = 8L;

        String motivo = "Tecnico anterior no disponible";

        AsignarSolicitudRequest request = new AsignarSolicitudRequest();
        request.setIdTecnico(idTecnico);
        request.setIdGrupo(null);
        request.setIdPrioridad(1);
        request.setMotivoReasignacion(motivo);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idAdministrador.toString(),
                        null
                );

        ResponseEntity<?> respuesta =
                controller.asignar(
                        idSolicitud,
                        request,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        verify(solicitudRepository).asignarSolicitud(
                idSolicitud,
                idAdministrador,
                idTecnico,
                null,
                1,
                motivo
        );
    }

    @Test
    void usaAdministradorDelJwtYNoDelBody() {

        Long idSolicitud = 103L;
        Long idAdministradorJwt = 77L;

        AsignarSolicitudRequest request = new AsignarSolicitudRequest();
        request.setIdTecnico(9L);
        request.setIdGrupo(null);
        request.setIdPrioridad(3);
        request.setMotivoReasignacion(null);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idAdministradorJwt.toString(),
                        null
                );

        controller.asignar(
                idSolicitud,
                request,
                authentication
        );

        verify(solicitudRepository).asignarSolicitud(
                eq(idSolicitud),
                eq(idAdministradorJwt),
                eq(9L),
                isNull(),
                eq(3),
                isNull()
        );
    }

    @Test
    void recuperaSolicitudDespuesDeAsignarla() {

        Long idSolicitud = 104L;
        Long idAdministrador = 1L;

        AsignarSolicitudRequest request = new AsignarSolicitudRequest();
        request.setIdTecnico(10L);
        request.setIdGrupo(null);
        request.setIdPrioridad(1);
        request.setMotivoReasignacion(null);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idAdministrador.toString(),
                        null
                );

        controller.asignar(
                idSolicitud,
                request,
                authentication
        );

        verify(solicitudRepository).findById(idSolicitud);
    }
}