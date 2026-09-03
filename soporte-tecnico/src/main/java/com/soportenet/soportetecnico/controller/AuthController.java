package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.LoginRequest;
import com.soportenet.soportetecnico.dto.LoginResponse;
import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.EstadoCuenta;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import com.soportenet.soportetecnico.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soportenet.soportetecnico.service.AuditoriaSesionService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;


/**
 * Login (caso de uso 4.1.2 del documento). No hay endpoint de logout: al
 * ser JWT sin estado en el servidor, "cerrar sesion" es responsabilidad del
 * cliente (descartar el token); si se necesita invalidacion server-side mas
 * adelante, se apoya en la tabla auditoria_sesion que ya existe.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaSesionService auditoriaSesionService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditoriaSesionService auditoriaSesionService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaSesionService = auditoriaSesionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(request.getCorreo());

        boolean credencialesValidas = usuarioOpt.isPresent()
                && usuarioOpt.get().getContrasenaHash() != null
                && passwordEncoder.matches(request.getContrasena(), usuarioOpt.get().getContrasenaHash())
                && usuarioOpt.get().getEstadoCuenta() == EstadoCuenta.activo;

        if (!credencialesValidas) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Correo, contrasena o estado de cuenta invalidos."));
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtService.generarToken(
                usuario.getIdUsuario(),
                usuario.getRol().name(),
                usuario.getCorreo()
        );

        String ipOrigen = httpRequest.getRemoteAddr();
        auditoriaSesionService.abrirSesion(
                usuario.getIdUsuario(),
                ipOrigen
        );

        // El enum RolUsuario esta declarado en minusculas (coincide con el
        // ENUM de Postgres), pero el frontend y hasRole(...) del backend
        // trabajan con el rol en mayusculas (ROLE_SUPERUSUARIO, etc.) - se
        // normaliza aqui para que el frontend no tenga que adivinar el caso.
        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        usuario.getIdUsuario(),
                        usuario.getRol().name().toUpperCase()
                )
        );
    }
}