package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.ActivarCuentaRequest;
import com.soportenet.soportetecnico.dto.CambiarEstadoCuentaRequest;
import com.soportenet.soportetecnico.dto.InvitacionResponse;
import com.soportenet.soportetecnico.dto.InvitarUsuarioRequest;
import com.soportenet.soportetecnico.dto.UsuarioResponse;
import com.soportenet.soportetecnico.entity.Usuario;
import com.soportenet.soportetecnico.enums.RolUsuario;
import com.soportenet.soportetecnico.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Invitacion y activacion de cuentas.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final int DIAS_VALIDEZ_TOKEN_DEFAULT = 7;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @PostMapping("/invitaciones")
    @Transactional
    public ResponseEntity<InvitacionResponse> invitar(
            @Valid @RequestBody InvitarUsuarioRequest request,
            Authentication authentication) {

        Long idSuperusuario = Long.valueOf(authentication.getName());

        Integer diasValidez = request.getDiasValidezToken() != null
                ? request.getDiasValidezToken()
                : DIAS_VALIDEZ_TOKEN_DEFAULT;

        // Crear la invitación y generar el token
        String token = usuarioRepository.invitarUsuario(
                idSuperusuario,
                request.getNombreUsuario(),
                request.getCorreo(),
                request.getRol().name(),
                diasValidez
        );

        // ==========================================
        // ENVIAR CORREO DE INVITACIÓN
        // ==========================================

        try {

            SimpleMailMessage mensaje = new SimpleMailMessage();

            mensaje.setFrom("soportenet000@gmail.com");
            mensaje.setTo(request.getCorreo());

            mensaje.setSubject("Invitación para activar tu cuenta - SoporteNet");

            String enlaceActivacion =
                    "http://localhost:8080/activar.html?token=" + token;

            String contenido =
                    "Hola " + request.getNombreUsuario() + ",\n\n" +

                            "Has recibido una invitación para crear tu cuenta en SoporteNet.\n\n" +

                            "Rol asignado: " + request.getRol().name() + "\n\n" +

                            "Para activar tu cuenta y establecer tu contraseña, " +
                            "ingresa al siguiente enlace:\n\n" +

                            enlaceActivacion + "\n\n" +

                            "Este enlace tiene una validez de " +
                            diasValidez + " días.\n\n" +

                            "Si no solicitaste esta invitación, puedes ignorar este correo.\n\n" +

                            "Saludos,\n" +
                            "Equipo SoporteNet";

            mensaje.setText(contenido);

            mailSender.send(mensaje);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "La invitación fue creada, pero no se pudo enviar el correo: "
                            + e.getMessage(),
                    e
            );
        }

        // ==========================================
        // RESPUESTA
        // ==========================================

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new InvitacionResponse(
                        request.getCorreo(),
                        token
                ));
    }

    @PostMapping("/activacion")
    @Transactional
    public ResponseEntity<UsuarioResponse> activar(
            @Valid @RequestBody ActivarCuentaRequest request) {

        String hash = passwordEncoder.encode(request.getContrasena());

        Long idUsuario = usuarioRepository.activarCuenta(
                request.getToken(),
                hash
        );

        Usuario activado = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalStateException(
                        "La cuenta se activo pero no se pudo recuperar (id="
                                + idUsuario + ")"
                ));

        return ResponseEntity.ok(
                UsuarioResponse.fromEntity(activado)
        );
    }

    /**
     * Superusuario: activa, suspende o desactiva una cuenta.
     */
    @PostMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoCuentaRequest request,
            Authentication authentication) {

        Long idSuperusuario = Long.valueOf(authentication.getName());

        usuarioRepository.cambiarEstadoCuenta(
                idSuperusuario,
                id,
                request.getEstadoCuenta().name()
        );

        Usuario actualizado = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario cambio de estado pero no se pudo recuperar (id="
                                + id + ")"
                ));

        return ResponseEntity.ok(
                UsuarioResponse.fromEntity(actualizado)
        );
    }

    /**
     * Superusuario: listado de usuarios, filtrable por rol.
     */
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @RequestParam(required = false) RolUsuario rol,
            @PageableDefault(
                    size = 20,
                    sort = "nombreUsuario"
            ) Pageable pageable) {

        Page<Usuario> pagina = (rol != null)
                ? usuarioRepository.findByRol(rol, pageable)
                : usuarioRepository.findAll(pageable);

        return ResponseEntity.ok(
                pagina.map(UsuarioResponse::fromEntity)
        );
    }
}