package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.ConfirmarClienteRequest;
import com.soportenet.soportetecnico.dto.CrearSolicitudRequest;
import com.soportenet.soportetecnico.dto.SolicitudResponse;
import com.soportenet.soportetecnico.entity.Solicitud;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudRepository solicitudRepository;

    public SolicitudController(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<SolicitudResponse> crear(
            @Valid @RequestBody CrearSolicitudRequest request,
            Authentication authentication) {

        Long idCliente = Long.valueOf(authentication.getName());

        Long idSolicitud = solicitudRepository.crearSolicitud(
                idCliente,
                request.getDescripcion(),
                request.getIdCategoria()
        );

        Solicitud creada = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalStateException(
                        "La solicitud se creo pero no se pudo recuperar (id="
                                + idSolicitud + ")"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SolicitudResponse.fromEntity(creada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(
            @PathVariable Long id,
            Authentication authentication) {

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElse(null);

        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }

        if (tieneRol(authentication, "ADMINISTRADOR")
                || tieneRol(authentication, "SUPERUSUARIO")) {

            return ResponseEntity.ok(
                    SolicitudResponse.fromEntity(solicitud)
            );
        }

        Long idUsuario = Long.valueOf(authentication.getName());

        if (tieneRol(authentication, "CLIENTE")) {

            if (solicitud.getCliente() != null
                    && solicitud.getCliente().getIdUsuario() != null
                    && solicitud.getCliente()
                    .getIdUsuario()
                    .equals(idUsuario)) {

                return ResponseEntity.ok(
                        SolicitudResponse.fromEntity(solicitud)
                );
            }

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        if (tieneRol(authentication, "TECNICO")) {

            boolean tieneAcceso =
                    solicitudRepository.tecnicoTieneAcceso(
                            id,
                            idUsuario
                    );

            if (tieneAcceso) {
                return ResponseEntity.ok(
                        SolicitudResponse.fromEntity(solicitud)
                );
            }

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    @GetMapping
    public ResponseEntity<Page<SolicitudResponse>> listar(
            @RequestParam(required = false) String estado,
            @PageableDefault(
                    size = 20,
                    sort = "fechaCreacion",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            Authentication authentication) {

        Page<Solicitud> pagina;

        if (tieneRol(authentication, "ADMINISTRADOR")
                || tieneRol(authentication, "SUPERUSUARIO")) {

            pagina = (estado != null)
                    ? solicitudRepository
                    .findByEstadoNombreEstado(estado, pageable)
                    : solicitudRepository
                    .findAll(pageable);

        } else {

            Long idCliente = Long.valueOf(authentication.getName());

            pagina = (estado != null)
                    ? solicitudRepository
                    .findByClienteIdUsuarioAndEstadoNombreEstado(
                            idCliente,
                            estado,
                            pageable
                    )
                    : solicitudRepository
                    .findByClienteIdUsuario(
                            idCliente,
                            pageable
                    );
        }

        return ResponseEntity.ok(
                pagina.map(SolicitudResponse::fromEntity)
        );
    }

    @GetMapping("/mis-tareas")
    public ResponseEntity<Page<SolicitudResponse>> misTareas(
            @PageableDefault(size = 20)
            Pageable pageable,
            Authentication authentication) {

        Long idTecnico = Long.valueOf(authentication.getName());

        Page<Solicitud> pagina =
                solicitudRepository.findMisTareas(
                        idTecnico,
                        pageable
                );

        return ResponseEntity.ok(
                pagina.map(SolicitudResponse::fromEntity)
        );
    }

    @PostMapping("/{id}/confirmacion")
    @Transactional
    public ResponseEntity<SolicitudResponse> confirmar(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarClienteRequest request,
            Authentication authentication) {

        Long idCliente = Long.valueOf(authentication.getName());

        solicitudRepository.confirmarCliente(
                id,
                idCliente,
                request.getProblemaResuelto()
        );

        Solicitud actualizada =
                solicitudRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "La solicitud se confirmo pero no se pudo recuperar "
                                                + "(id=" + id + ")"
                                )
                        );

        return ResponseEntity.ok(
                SolicitudResponse.fromEntity(actualizada)
        );
    }

    private boolean tieneRol(
            Authentication authentication,
            String rol) {

        String authority = "ROLE_" + rol;

        for (GrantedAuthority ga :
                authentication.getAuthorities()) {

            if (ga.getAuthority().equals(authority)) {
                return true;
            }
        }

        return false;
    }
}