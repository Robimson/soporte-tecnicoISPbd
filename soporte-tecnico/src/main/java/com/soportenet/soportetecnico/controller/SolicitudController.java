package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.ConfirmarClienteRequest;
import com.soportenet.soportetecnico.dto.CrearSolicitudRequest;
import com.soportenet.soportetecnico.dto.SolicitudResponse;
import com.soportenet.soportetecnico.entity.Adjunto;
import com.soportenet.soportetecnico.entity.Solicitud;
import com.soportenet.soportetecnico.repository.AdjuntoRepository;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import com.soportenet.soportetecnico.service.AdjuntoService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.soportenet.soportetecnico.dto.ResumenTecnicoDTO;
import java.time.OffsetDateTime;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudRepository solicitudRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final AdjuntoService adjuntoService;

    public SolicitudController(
            SolicitudRepository solicitudRepository,
            AdjuntoRepository adjuntoRepository,
            AdjuntoService adjuntoService
    ) {
        this.solicitudRepository = solicitudRepository;
        this.adjuntoRepository = adjuntoRepository;
        this.adjuntoService = adjuntoService;
    }

    // ============================================================
    // CREAR SOLICITUD
    // ============================================================

    @PostMapping
    @Transactional
    public ResponseEntity<?> crear(
            @Valid @RequestBody CrearSolicitudRequest request,
            Authentication authentication
    ) {

        try {

            Long idCliente =
                    Long.valueOf(authentication.getName());

            Long idSolicitud =
                    solicitudRepository.crearSolicitud(
                            idCliente,
                            request.getDescripcion(),
                            request.getIdCategoria()
                    );

            Solicitud solicitud =
                    solicitudRepository.findById(idSolicitud)
                            .orElse(null);

            if (solicitud == null) {

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(
                                Map.of(
                                        "mensaje",
                                        "La solicitud fue creada pero no pudo recuperarse."
                                )
                        );
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            SolicitudResponse.fromEntity(solicitud)
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "mensaje",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "No se pudo crear la solicitud."
                            )
                    );
        }
    }

    // ============================================================
    // SUBIR EVIDENCIA
    // ============================================================

    @PostMapping("/{id}/adjuntos")
    public ResponseEntity<?> subirEvidencia(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication
    ) {

        try {

            Long idUsuario =
                    Long.valueOf(authentication.getName());

            Long idAdjunto =
                    adjuntoService.guardarEvidencia(
                            id,
                            idUsuario,
                            archivo
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "Evidencia adjuntada correctamente.",
                                    "idAdjunto",
                                    idAdjunto
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "mensaje",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No se pudo guardar la evidencia."
                            )
                    );
        }
    }

    // ============================================================
    // VER ADJUNTOS DE UNA SOLICITUD
    // ADMINISTRADOR / SUPERUSUARIO
    // ============================================================

    @GetMapping("/{id}/adjuntos")
    public ResponseEntity<?> listarAdjuntos(
            @PathVariable Long id,
            Authentication authentication
    ) {

        if (authentication == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No hay una sesión autenticada."
                            )
                    );
        }

        if (!tieneRol(authentication, "ADMINISTRADOR")
                && !tieneRol(authentication, "SUPERUSUARIO")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No tienes permisos para consultar las evidencias."
                            )
                    );
        }

        if (!solicitudRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        try {

            List<Adjunto> adjuntos =
                    adjuntoRepository.findByIdSolicitud(id);

            return ResponseEntity.ok(adjuntos);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No se pudieron consultar las evidencias."
                            )
                    );
        }
    }

    // ============================================================
    // ABRIR / DESCARGAR EVIDENCIA
    // ADMINISTRADOR / SUPERUSUARIO
    // ============================================================

    @GetMapping("/adjuntos/{idAdjunto}/archivo")
    public ResponseEntity<?> abrirArchivo(
            @PathVariable Long idAdjunto,
            Authentication authentication
    ) {

        if (authentication == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No hay una sesión autenticada."
                            )
                    );
        }

        if (!tieneRol(authentication, "ADMINISTRADOR")
                && !tieneRol(authentication, "SUPERUSUARIO")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No tienes permisos para ver esta evidencia."
                            )
                    );
        }

        try {

            Adjunto adjunto =
                    adjuntoRepository.findById(idAdjunto)
                            .orElse(null);

            if (adjunto == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            String url =
                    adjunto.getUrlAlmacenamiento();

            if (url == null || url.trim().isEmpty()) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                                Map.of(
                                        "mensaje",
                                        "El archivo no tiene una ubicación registrada."
                                )
                        );
            }

            /*
             * La base de datos guarda:
             *
             * /uploads/evidencias/archivo.pdf
             *
             * Nosotros necesitamos obtener:
             *
             * uploads/evidencias/archivo.pdf
             */

            String rutaArchivo =
                    url.startsWith("/")
                            ? url.substring(1)
                            : url;

            Path archivo =
                    Paths.get(rutaArchivo)
                            .toAbsolutePath()
                            .normalize();

            Resource resource =
                    new UrlResource(
                            archivo.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(
                                Map.of(
                                        "mensaje",
                                        "El archivo no existe en el servidor."
                                )
                        );
            }

            MediaType mediaType;

            try {

                mediaType =
                        MediaType.parseMediaType(
                                adjunto.getTipoArchivo()
                        );

            } catch (Exception e) {

                mediaType =
                        MediaType.APPLICATION_OCTET_STREAM;
            }

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    mediaType
            );

            /*
             * INLINE es importante.
             *
             * Para PDF:
             * el navegador intenta mostrarlo
             * directamente en una pestaña.
             *
             * Para imágenes:
             * también se muestran directamente.
             */

            headers.setContentDisposition(
                    ContentDisposition
                            .inline()
                            .filename(
                                    adjunto.getNombreArchivo()
                            )
                            .build()
            );

            headers.setContentLength(
                    resource.contentLength()
            );

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(resource);

        } catch (MalformedURLException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "La ruta del archivo no es válida."
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "mensaje",
                                    "No se pudo abrir la evidencia."
                            )
                    );
        }
    }

    // ============================================================
    // OBTENER UNA SOLICITUD
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Solicitud solicitud =
                solicitudRepository.findById(id)
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

        Long idUsuario =
                Long.valueOf(authentication.getName());

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

            boolean acceso =
                    solicitudRepository.tecnicoTieneAcceso(
                            id,
                            idUsuario
                    );

            if (acceso) {

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

    // ============================================================
    // LISTAR SOLICITUDES
    // ============================================================

    @GetMapping
    public ResponseEntity<Page<SolicitudResponse>> listar(
            @RequestParam(required = false) String estado,

            @PageableDefault(
                    size = 20,
                    sort = "fechaCreacion",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,

            Authentication authentication
    ) {

        Page<Solicitud> pagina;

        if (tieneRol(authentication, "ADMINISTRADOR")
                || tieneRol(authentication, "SUPERUSUARIO")) {

            pagina =
                    estado != null
                            ?
                            solicitudRepository
                                    .findByEstadoNombreEstado(
                                            estado,
                                            pageable
                                    )
                            :
                            solicitudRepository
                                    .findAll(pageable);

        } else {

            Long idCliente =
                    Long.valueOf(authentication.getName());

            pagina =
                    estado != null
                            ?
                            solicitudRepository
                                    .findByClienteIdUsuarioAndEstadoNombreEstado(
                                            idCliente,
                                            estado,
                                            pageable
                                    )
                            :
                            solicitudRepository
                                    .findByClienteIdUsuario(
                                            idCliente,
                                            pageable
                                    );
        }

        return ResponseEntity.ok(
                pagina.map(
                        SolicitudResponse::fromEntity
                )
        );
    }

    // ============================================================
    // MIS TAREAS
    // ============================================================

    @GetMapping("/mis-tareas")
    public ResponseEntity<Page<SolicitudResponse>> misTareas(
            @PageableDefault(size = 20)
            Pageable pageable,

            Authentication authentication
    ) {

        Long idTecnico =
                Long.valueOf(authentication.getName());

        Page<Solicitud> pagina =
                solicitudRepository.findMisTareas(
                        idTecnico,
                        pageable
                );

        return ResponseEntity.ok(
                pagina.map(
                        SolicitudResponse::fromEntity
                )
        );
    }



    @GetMapping("/mis-tareas/resumen")
    public ResponseEntity<ResumenTecnicoDTO> resumenMisTareas(
            Authentication authentication
    ) {

        Long idTecnico = Long.valueOf(authentication.getName());

        OffsetDateTime inicioHoy = OffsetDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        ResumenTecnicoDTO dto = new ResumenTecnicoDTO();
        dto.setEnProceso(solicitudRepository.contarMisTareasPorEstado(idTecnico, "En Proceso"));
        dto.setPendientes(solicitudRepository.contarMisTareasPorEstado(idTecnico, "Pendiente"));
        dto.setResueltasHoy(solicitudRepository.contarHistorialPorEstadoDesde(idTecnico, "Resuelta%", inicioHoy));
        dto.setTotalCerradas(solicitudRepository.contarHistorialPorEstado(idTecnico, "Cerrada"));

        return ResponseEntity.ok(dto);
    }



    // ============================================================
    // CONFIRMAR SOLICITUD
    // ============================================================

    @PostMapping("/{id}/confirmacion")
    @Transactional
    public ResponseEntity<SolicitudResponse> confirmar(
            @PathVariable Long id,

            @Valid
            @RequestBody ConfirmarClienteRequest request,

            Authentication authentication
    ) {

        Long idCliente =
                Long.valueOf(authentication.getName());

        solicitudRepository.confirmarCliente(
                id,
                idCliente,
                request.getProblemaResuelto()
        );

        Solicitud actualizada =
                solicitudRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "La solicitud se confirmó pero no se pudo recuperar."
                                )
                        );

        return ResponseEntity.ok(
                SolicitudResponse.fromEntity(
                        actualizada
                )
        );
    }

    // ============================================================
    // COMPROBAR ROL
    // ============================================================

    private boolean tieneRol(
            Authentication authentication,
            String rol
    ) {

        if (authentication == null) {
            return false;
        }

        String authority =
                "ROLE_" + rol;

        for (GrantedAuthority ga :
                authentication.getAuthorities()) {

            if (authority.equals(
                    ga.getAuthority())) {

                return true;
            }
        }

        return false;
    }
}