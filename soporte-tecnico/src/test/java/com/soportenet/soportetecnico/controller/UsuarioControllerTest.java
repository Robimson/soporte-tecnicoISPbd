package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.ActivarCuentaRequest;
import com.soportenet.soportetecnico.dto.CambiarEstadoCuentaRequest;
import com.soportenet.soportetecnico.dto.InvitarUsuarioRequest;
import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.RolUsuario;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioController usuarioController;

    @BeforeEach
    void configurar() {

        usuarioRepository =
                Mockito.mock(UsuarioRepository.class);

        passwordEncoder =
                Mockito.mock(PasswordEncoder.class);

        usuarioController =
                new UsuarioController(
                        usuarioRepository,
                        passwordEncoder
                );
    }

    @Test
    void invitacionUsaSuperusuarioDelJwt() {

        Long idSuperusuario = 1L;

        InvitarUsuarioRequest request =
                new InvitarUsuarioRequest();

        request.setNombreUsuario("Tecnico Uno");
        request.setCorreo("tecnico@isp.com");
        request.setRol(RolUsuario.tecnico);
        request.setDiasValidezToken(5);

        when(usuarioRepository.invitarUsuario(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn("TOKEN-DE-PRUEBA");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idSuperusuario.toString(),
                        null
                );

        usuarioController.invitar(
                request,
                authentication
        );

        verify(usuarioRepository)
                .invitarUsuario(
                        idSuperusuario,
                        "Tecnico Uno",
                        "tecnico@isp.com",
                        RolUsuario.tecnico.name(),
                        5
                );
    }

    @Test
    void invitacionUsaSieteDiasPorDefecto() {

        Long idSuperusuario = 2L;

        InvitarUsuarioRequest request =
                new InvitarUsuarioRequest();

        request.setNombreUsuario("Cliente Uno");
        request.setCorreo("cliente@isp.com");
        request.setRol(RolUsuario.cliente);
        request.setDiasValidezToken(null);

        when(usuarioRepository.invitarUsuario(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn("TOKEN");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idSuperusuario.toString(),
                        null
                );

        usuarioController.invitar(
                request,
                authentication
        );

        verify(usuarioRepository)
                .invitarUsuario(
                        idSuperusuario,
                        "Cliente Uno",
                        "cliente@isp.com",
                        RolUsuario.cliente.name(),
                        7
                );
    }

    @Test
    void activacionCifraContrasenaAntesDeGuardar() {

        ActivarCuentaRequest request =
                new ActivarCuentaRequest();

        request.setToken("TOKEN-ACTIVACION");
        request.setContrasena("ClaveTemporal123");

        when(passwordEncoder.encode(
                "ClaveTemporal123"
        )).thenReturn("$2a$HASH-DE-PRUEBA");

        when(usuarioRepository.activarCuenta(
                "TOKEN-ACTIVACION",
                "$2a$HASH-DE-PRUEBA"
        )).thenReturn(10L);

        Usuario usuario = new Usuario();

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuario));

        usuarioController.activar(request);

        verify(passwordEncoder)
                .encode("ClaveTemporal123");

        verify(usuarioRepository)
                .activarCuenta(
                        "TOKEN-ACTIVACION",
                        "$2a$HASH-DE-PRUEBA"
                );
    }

    @Test
    void cambioEstadoUsaSuperusuarioDelJwt() {

        Long idSuperusuario = 3L;
        Long idUsuarioObjetivo = 20L;

        CambiarEstadoCuentaRequest request =
                new CambiarEstadoCuentaRequest();

        /*
         * Si esta linea aparece en rojo, dime los valores
         * exactos de tu enum EstadoCuenta y la adaptamos.
         */
        request.setEstadoCuenta(
                com.soportenet.soportetecnico.enums.EstadoCuenta.activo
        );

        Usuario usuario = new Usuario();

        when(usuarioRepository.findById(idUsuarioObjetivo))
                .thenReturn(Optional.of(usuario));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        idSuperusuario.toString(),
                        null
                );

        usuarioController.cambiarEstado(
                idUsuarioObjetivo,
                request,
                authentication
        );

        verify(usuarioRepository)
                .cambiarEstadoCuenta(
                        idSuperusuario,
                        idUsuarioObjetivo,
                        request.getEstadoCuenta().name()
                );
    }
}
