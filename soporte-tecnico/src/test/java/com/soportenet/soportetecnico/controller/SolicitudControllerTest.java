package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.entity.Cliente;
import com.soportenet.soportetecnico.entity.Solicitud;
import com.soportenet.soportetecnico.repository.AdjuntoRepository;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import com.soportenet.soportetecnico.service.AdjuntoService;
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

class SolicitudControllerTest {

    private SolicitudRepository solicitudRepository;
    private AdjuntoRepository adjuntoRepository;
    private AdjuntoService adjuntoService;

    private SolicitudController solicitudController;

    @BeforeEach
    void configurar() {

        solicitudRepository =
                Mockito.mock(SolicitudRepository.class);

        adjuntoRepository =
                Mockito.mock(AdjuntoRepository.class);

        adjuntoService =
                Mockito.mock(AdjuntoService.class);

        solicitudController =
                new SolicitudController(
                        solicitudRepository,
                        adjuntoRepository,
                        adjuntoService
                );
    }

    @Test
    void clienteNoPuedeVerSolicitudDeOtroCliente() {

        Long idSolicitud = 10L;
        Long idClienteAutenticado = 1L;
        Long idClientePropietario = 2L;

        Cliente clientePropietario = new Cliente();
        clientePropietario.setIdUsuario(idClientePropietario);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);
        solicitud.setCliente(clientePropietario);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idClienteAutenticado.toString(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CLIENTE"
                                )
                        )
                );

        ResponseEntity<?> respuesta =
                solicitudController.obtener(
                        idSolicitud,
                        authentication
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                respuesta.getStatusCode()
        );
    }

    @Test
    void clientePuedeVerSuPropiaSolicitud() {

        Long idSolicitud = 11L;
        Long idCliente = 1L;

        Cliente cliente = new Cliente();
        cliente.setIdUsuario(idCliente);

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);
        solicitud.setCliente(cliente);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idCliente.toString(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CLIENTE"
                                )
                        )
                );

        ResponseEntity<?> respuesta =
                solicitudController.obtener(
                        idSolicitud,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    @Test
    void tecnicoAsignadoPuedeVerSolicitud() {

        Long idSolicitud = 20L;
        Long idTecnico = 5L;

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        when(solicitudRepository.tecnicoTieneAcceso(
                idSolicitud,
                idTecnico
        )).thenReturn(true);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idTecnico.toString(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_TECNICO"
                                )
                        )
                );

        ResponseEntity<?> respuesta =
                solicitudController.obtener(
                        idSolicitud,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    @Test
    void tecnicoNoAsignadoNoPuedeVerSolicitud() {

        Long idSolicitud = 21L;
        Long idTecnico = 6L;

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        when(solicitudRepository.tecnicoTieneAcceso(
                idSolicitud,
                idTecnico
        )).thenReturn(false);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idTecnico.toString(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_TECNICO"
                                )
                        )
                );

        ResponseEntity<?> respuesta =
                solicitudController.obtener(
                        idSolicitud,
                        authentication
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                respuesta.getStatusCode()
        );
    }

    @Test
    void administradorPuedeVerCualquierSolicitud() {

        Long idSolicitud = 30L;
        Long idAdministrador = 100L;

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idAdministrador.toString(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMINISTRADOR"
                                )
                        )
                );

        ResponseEntity<?> respuesta =
                solicitudController.obtener(
                        idSolicitud,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }

    @Test
    void superusuarioPuedeVerCualquierSolicitud() {

        Long idSolicitud = 40L;
        Long idSuperusuario = 200L;

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idSolicitud);

        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitud));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idSuperusuario.toString(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_SUPERUSUARIO"
                                )
                        )
                );

        ResponseEntity<?> respuesta =
                solicitudController.obtener(
                        idSolicitud,
                        authentication
                );

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );
    }
}