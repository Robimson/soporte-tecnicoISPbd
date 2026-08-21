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

/**
 * Invitacion y activacion de cuentas (casos de uso 4.4.2 y 4.1.1 del
 * documento). El Superusuario nunca crea contrasenas (seccion 2.4): solo
 * genera un token de invitacion; el hash de la contrasena se calcula aqui
 * en el backend (BCrypt) recien cuando el propio usuario la define al
 * activar su cuenta.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final int DIAS_VALIDEZ_TOKEN_DEFAULT = 7;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/invitaciones")
    @Transactional
    public ResponseEntity<InvitacionResponse> invitar(@Valid @RequestBody InvitarUsuarioRequest request,
                                                        Authentication authentication) {

        Long idSuperusuario = Long.valueOf(authentication.getName());

        Integer diasValidez = request.getDiasValidezToken() != null
                ? request.getDiasValidezToken()
                : DIAS_VALIDEZ_TOKEN_DEFAULT;

        String token = usuarioRepository.invitarUsuario(
                idSuperusuario,
                request.getNombreUsuario(),
                request.getCorreo(),
                request.getRol().name(),
                diasValidez
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new InvitacionResponse(request.getCorreo(), token));
    }

    @PostMapping("/activacion")
    @Transactional
    public ResponseEntity<UsuarioResponse> activar(@Valid @RequestBody ActivarCuentaRequest request) {

        String hash = passwordEncoder.encode(request.getContrasena());

        Long idUsuario = usuarioRepository.activarCuenta(request.getToken(), hash);

        Usuario activado = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalStateException(
                        "La cuenta se activo pero no se pudo recuperar (id=" + idUsuario + ")"));

        return ResponseEntity.ok(UsuarioResponse.fromEntity(activado));
    }

    /**
     * Superusuario: activa, suspende o desactiva una cuenta (caso de uso
     * 4.4.4 del documento). idSuperusuario sale del JWT.
     */
    @PostMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Long id,
                                                           @Valid @RequestBody CambiarEstadoCuentaRequest request,
                                                           Authentication authentication) {

        Long idSuperusuario = Long.valueOf(authentication.getName());

        usuarioRepository.cambiarEstadoCuenta(idSuperusuario, id, request.getEstadoCuenta().name());

        Usuario actualizado = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "El usuario cambio de estado pero no se pudo recuperar (id=" + id + ")"));

        return ResponseEntity.ok(UsuarioResponse.fromEntity(actualizado));
    }

    /**
     * Superusuario: listado de usuarios, filtrable por rol (caso de uso
     * 4.4.3 del documento).
     */
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @RequestParam(required = false) RolUsuario rol,
            @PageableDefault(size = 20, sort = "nombreUsuario") Pageable pageable) {

        Page<Usuario> pagina = (rol != null)
                ? usuarioRepository.findByRol(rol, pageable)
                : usuarioRepository.findAll(pageable);

        return ResponseEntity.ok(pagina.map(UsuarioResponse::fromEntity));
    }
}
