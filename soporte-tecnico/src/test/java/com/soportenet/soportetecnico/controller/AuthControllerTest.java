package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.LoginRequest;
import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.EstadoCuenta;
import com.soportenet.soportetecnico.enums.RolUsuario;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import com.soportenet.soportetecnico.security.JwtService;
import com.soportenet.soportetecnico.service.AuditoriaSesionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuditoriaSesionService auditoriaSesionService;
    private HttpServletRequest httpRequest;
    private AuthController authController;

    @BeforeEach
    void configurar() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtService = Mockito.mock(JwtService.class);
        auditoriaSesionService = Mockito.mock(AuditoriaSesionService.class);
        httpRequest = Mockito.mock(HttpServletRequest.class);

        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        authController = new AuthController(
                usuarioRepository,
                passwordEncoder,
                jwtService,
                auditoriaSesionService
        );
    }

    @Test
    void loginCorrectoDevuelveOk() {

        LoginRequest request = new LoginRequest();
        request.setCorreo("tecnico@isp.com");
        request.setContrasena("Clave123");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setCorreo("tecnico@isp.com");
        usuario.setContrasenaHash("$2a$hash");
        usuario.setEstadoCuenta(EstadoCuenta.activo);
        usuario.setRol(RolUsuario.tecnico);

        when(usuarioRepository.findByCorreo("tecnico@isp.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("Clave123", "$2a$hash"))
                .thenReturn(true);

        when(jwtService.generarToken(
                10L,
                "tecnico",
                "tecnico@isp.com"
        )).thenReturn("TOKEN_VALIDO");

        ResponseEntity<?> respuesta =
                authController.login(request, httpRequest);

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        verify(jwtService).generarToken(
                10L,
                "tecnico",
                "tecnico@isp.com"
        );

        verify(auditoriaSesionService).abrirSesion(
                10L,
                "127.0.0.1"
        );
    }

    @Test
    void contrasenaIncorrectaDevuelveUnauthorized() {

        LoginRequest request = new LoginRequest();
        request.setCorreo("tecnico@isp.com");
        request.setContrasena("ClaveMala");

        Usuario usuario = new Usuario();
        usuario.setContrasenaHash("$2a$hash");
        usuario.setEstadoCuenta(EstadoCuenta.activo);
        usuario.setRol(RolUsuario.tecnico);

        when(usuarioRepository.findByCorreo("tecnico@isp.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "ClaveMala",
                "$2a$hash"
        )).thenReturn(false);

        ResponseEntity<?> respuesta =
                authController.login(request, httpRequest);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );

        verify(jwtService, never())
                .generarToken(
                        anyLong(),
                        anyString(),
                        anyString()
                );

        verify(auditoriaSesionService, never())
                .abrirSesion(anyLong(), anyString());
    }

    @Test
    void usuarioSuspendidoNoPuedeIniciarSesion() {

        LoginRequest request = new LoginRequest();
        request.setCorreo("suspendido@isp.com");
        request.setContrasena("Clave123");

        Usuario usuario = new Usuario();
        usuario.setContrasenaHash("$2a$hash");
        usuario.setEstadoCuenta(EstadoCuenta.suspendido);
        usuario.setRol(RolUsuario.tecnico);

        when(usuarioRepository.findByCorreo("suspendido@isp.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "Clave123",
                "$2a$hash"
        )).thenReturn(true);

        ResponseEntity<?> respuesta =
                authController.login(request, httpRequest);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );
    }

    @Test
    void usuarioInactivoNoPuedeIniciarSesion() {

        LoginRequest request = new LoginRequest();
        request.setCorreo("inactivo@isp.com");
        request.setContrasena("Clave123");

        Usuario usuario = new Usuario();
        usuario.setContrasenaHash("$2a$hash");
        usuario.setEstadoCuenta(EstadoCuenta.inactivo);
        usuario.setRol(RolUsuario.cliente);

        when(usuarioRepository.findByCorreo("inactivo@isp.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "Clave123",
                "$2a$hash"
        )).thenReturn(true);

        ResponseEntity<?> respuesta =
                authController.login(request, httpRequest);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );
    }

    @Test
    void correoInexistenteDevuelveUnauthorized() {

        LoginRequest request = new LoginRequest();
        request.setCorreo("noexiste@isp.com");
        request.setContrasena("Clave123");

        when(usuarioRepository.findByCorreo("noexiste@isp.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> respuesta =
                authController.login(request, httpRequest);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }
}