package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.entity.Cliente;
import com.soportenet.soportetecnico.entity.ReporteSolicitud;
import com.soportenet.soportetecnico.entity.Solicitud;
import com.soportenet.soportetecnico.repository.ReporteSolicitudRepository;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ReporteControllerTest {

    private ReporteSolicitudRepository reporteSolicitudRepository;
    private SolicitudRepository solicitudRepository;
    private ReporteController reporteController;

    @BeforeEach
    void configurar() {

        reporteSolicitudRepository =
                Mockito.mock(ReporteSolicitudRepository.class);

        solicitudRepository =
                Mockito.mock(SolicitudRepository.class);

        reporteController =
                new ReporteController(
                        reporteSolicitudRepository,
                        solicitudRepository
                );
    }

    /**
     * CLIENTE:
     * no puede consultar el reporte de una solicitud
     * perteneciente a otro cliente.
     */
    @Test
    void clienteNoPuedeVerReporteDeOtroCliente() {

        Long idReporte = 1L;
        Long idClienteAutenticado = 10L;
        Long idClientePropietario = 20L;

        Cliente clientePropietario = new Cliente();
        clientePropietario.setIdUsuario(idClientePropietario);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(100L);
        solicitud.setCliente(clientePropietario);

        ReporteSolicitud reporte = new ReporteSolicitud();
        reporte.setIdReporte(idReporte);
        reporte.setSolicitud(solicitud);

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.of(reporte));

        Authentication authentication =
                crearAuthentication(
                        idClienteAutenticado,
                        "CLIENTE"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                respuesta.getStatusCode()
        );
    }

    /**
     * CLIENTE:
     * puede consultar el reporte de una solicitud propia.
     */
    @Test
    void clientePuedeVerReporteDeSuSolicitud() {

        Long idReporte = 2L;
        Long idCliente = 10L;

        Cliente cliente = new Cliente();
        cliente.setIdUsuario(idCliente);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(101L);
        solicitud.setCliente(cliente);

        ReporteSolicitud reporte = new ReporteSolicitud();
        reporte.setIdReporte(idReporte);
        reporte.setSolicitud(solicitud);

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.of(reporte));

        Authentication authentication =
                crearAuthentication(
                        idCliente,
                        "CLIENTE"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    /**
     * TECNICO:
     * puede consultar el reporte cuando actualmente
     * tiene acceso a la solicitud.
     */
    @Test
    void tecnicoAsignadoPuedeVerReporte() {

        Long idReporte = 3L;
        Long idSolicitud = 200L;
        Long idTecnico = 30L;

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        ReporteSolicitud reporte = new ReporteSolicitud();
        reporte.setIdReporte(idReporte);
        reporte.setSolicitud(solicitud);

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.of(reporte));

        when(solicitudRepository.tecnicoTieneAcceso(
                idSolicitud,
                idTecnico
        )).thenReturn(true);

        Authentication authentication =
                crearAuthentication(
                        idTecnico,
                        "TECNICO"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    /**
     * TECNICO:
     * no puede consultar el reporte cuando la solicitud
     * no esta asignada a el ni a uno de sus grupos.
     */
    @Test
    void tecnicoNoAsignadoNoPuedeVerReporte() {

        Long idReporte = 4L;
        Long idSolicitud = 201L;
        Long idTecnico = 31L;

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        ReporteSolicitud reporte = new ReporteSolicitud();
        reporte.setIdReporte(idReporte);
        reporte.setSolicitud(solicitud);

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.of(reporte));

        when(solicitudRepository.tecnicoTieneAcceso(
                idSolicitud,
                idTecnico
        )).thenReturn(false);

        Authentication authentication =
                crearAuthentication(
                        idTecnico,
                        "TECNICO"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                respuesta.getStatusCode()
        );
    }

    /**
     * ADMINISTRADOR:
     * puede consultar cualquier reporte.
     */
    @Test
    void administradorPuedeVerCualquierReporte() {

        Long idReporte = 5L;
        Long idAdministrador = 40L;

        ReporteSolicitud reporte = new ReporteSolicitud();
        reporte.setIdReporte(idReporte);

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.of(reporte));

        Authentication authentication =
                crearAuthentication(
                        idAdministrador,
                        "ADMINISTRADOR"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    /**
     * SUPERUSUARIO:
     * puede consultar cualquier reporte.
     */
    @Test
    void superusuarioPuedeVerCualquierReporte() {

        Long idReporte = 6L;
        Long idSuperusuario = 50L;

        ReporteSolicitud reporte = new ReporteSolicitud();
        reporte.setIdReporte(idReporte);

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.of(reporte));

        Authentication authentication =
                crearAuthentication(
                        idSuperusuario,
                        "SUPERUSUARIO"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    /**
     * Si el reporte no existe debe responder 404.
     */
    @Test
    void reporteInexistenteDevuelveNotFound() {

        Long idReporte = 999L;

        when(reporteSolicitudRepository.findById(idReporte))
                .thenReturn(Optional.empty());

        Authentication authentication =
                crearAuthentication(
                        40L,
                        "ADMINISTRADOR"
                );

        ResponseEntity<?> respuesta =
                reporteController.obtener(
                        idReporte,
                        authentication
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );
    }

    /**
     * Metodo auxiliar para crear usuarios autenticados
     * con diferentes roles durante las pruebas.
     */
    private Authentication crearAuthentication(
            Long idUsuario,
            String rol
    ) {

        return new UsernamePasswordAuthenticationToken(
                idUsuario.toString(),
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + rol
                        )
                )
        );
    }
}