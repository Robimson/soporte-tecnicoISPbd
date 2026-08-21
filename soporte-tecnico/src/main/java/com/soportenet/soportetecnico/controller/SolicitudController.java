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

    /**
     * Cliente crea una nueva solicitud de soporte.
     * Toda la logica de negocio (validaciones, estado inicial "Pendiente")
     * vive en sp_crear_solicitud dentro de PostgreSQL; este endpoint solo
     * la invoca y devuelve el ticket recien creado. idCliente sale del JWT
     * (SecurityConfig exige rol CLIENTE para este endpoint), no del body.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody CrearSolicitudRequest request,
                                                     Authentication authentication) {

        Long idCliente = Long.valueOf(authentication.getName());

        Long idSolicitud = solicitudRepository.crearSolicitud(
                idCliente,
                request.getDescripcion(),
                request.getIdCategoria()
        );

        Solicitud creada = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalStateException(
                        "La solicitud se creo pero no se pudo recuperar (id=" + idSolicitud + ")"));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SolicitudResponse.fromEntity(creada));
    }

    /**
     * Consulta el detalle de una solicitud (caso de uso 4.1.5 del documento).
     */
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id) {
        return solicitudRepository.findById(id)
                .map(SolicitudResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Cliente: lista sus propias solicitudes, opcionalmente filtradas por
     * estado (caso de uso 4.1.4). Administrador/Superusuario: lista TODAS
     * las solicitudes, opcionalmente filtradas por estado (caso de uso
     * 4.3.3). Paginado (seccion 6.3): 20 filas por defecto.
     */
    @GetMapping
    public ResponseEntity<Page<SolicitudResponse>> listar(
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Page<Solicitud> pagina;

        if (tieneRol(authentication, "ADMINISTRADOR") || tieneRol(authentication, "SUPERUSUARIO")) {
            pagina = (estado != null)
                    ? solicitudRepository.findByEstadoNombreEstado(estado, pageable)
                    : solicitudRepository.findAll(pageable);
        } else {
            Long idCliente = Long.valueOf(authentication.getName());
            pagina = (estado != null)
                    ? solicitudRepository.findByClienteIdUsuarioAndEstadoNombreEstado(idCliente, estado, pageable)
                    : solicitudRepository.findByClienteIdUsuario(idCliente, pageable);
        }

        return ResponseEntity.ok(pagina.map(SolicitudResponse::fromEntity));
    }

    /**
     * Tecnico: "Mis tareas" (caso de uso 4.2.2) - solicitudes asignadas a el
     * o a su grupo, ordenadas por prioridad.
     */
    @GetMapping("/mis-tareas")
    public ResponseEntity<Page<SolicitudResponse>> misTareas(@PageableDefault(size = 20) Pageable pageable,
                                                               Authentication authentication) {

        Long idTecnico = Long.valueOf(authentication.getName());
        Page<Solicitud> pagina = solicitudRepository.findMisTareas(idTecnico, pageable);

        return ResponseEntity.ok(pagina.map(SolicitudResponse::fromEntity));
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        String authority = "ROLE_" + rol;
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if (ga.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cliente confirma o rechaza la solucion de su ticket, una vez que esta
     * "Resuelta - Pendiente Confirmacion del Cliente" (seccion 3.1 del
     * documento). Si confirma, el ticket se cierra; si indica que el
     * problema persiste, se reabre y vuelve a "En Proceso". idCliente sale
     * del JWT; sp_confirmar_cliente ademas valida que sea el dueno real de
     * la solicitud.
     */
    @PostMapping("/{id}/confirmacion")
    @Transactional
    public ResponseEntity<SolicitudResponse> confirmar(@PathVariable Long id,
                                                          @Valid @RequestBody ConfirmarClienteRequest request,
                                                          Authentication authentication) {

        Long idCliente = Long.valueOf(authentication.getName());

        solicitudRepository.confirmarCliente(id, idCliente, request.getProblemaResuelto());

        Solicitud actualizada = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "La solicitud se confirmo pero no se pudo recuperar (id=" + id + ")"));

        return ResponseEntity.ok(SolicitudResponse.fromEntity(actualizada));
    }
}
