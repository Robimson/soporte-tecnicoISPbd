package com.soportenet.soportetecnico.service;

import com.soportenet.soportetecnico.repository.AdjuntoRepository;
import com.soportenet.soportetecnico.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AdjuntoService {

    private static final long MAX_TAMANO = 10 * 1024 * 1024;

    private final AdjuntoRepository adjuntoRepository;
    private final SolicitudRepository solicitudRepository;

    private final Path carpetaEvidencias =
            Paths.get("uploads", "evidencias");

    public AdjuntoService(
            AdjuntoRepository adjuntoRepository,
            SolicitudRepository solicitudRepository) {

        this.adjuntoRepository = adjuntoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public Long guardarEvidencia(
            Long idSolicitud,
            Long idUsuario,
            MultipartFile archivo) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se recibió ningún archivo."
            );
        }

        if (archivo.getSize() > MAX_TAMANO) {
            throw new IllegalArgumentException(
                    "El archivo no puede superar los 10 MB."
            );
        }

        String tipo = archivo.getContentType();

        if (tipo == null ||
                (!tipo.equalsIgnoreCase("image/jpeg")
                        && !tipo.equalsIgnoreCase("image/png")
                        && !tipo.equalsIgnoreCase("image/jpg")
                        && !tipo.equalsIgnoreCase("image/webp")
                        && !tipo.equalsIgnoreCase("application/pdf"))) {

            throw new IllegalArgumentException(
                    "Solo se permiten imágenes o archivos PDF."
            );
        }

        if (adjuntoRepository.countByIdSolicitud(idSolicitud) >= 5) {
            throw new IllegalArgumentException(
                    "La solicitud ya tiene el máximo de 5 evidencias."
            );
        }

        if (!solicitudRepository.existsById(idSolicitud)) {
            throw new IllegalArgumentException(
                    "La solicitud no existe."
            );
        }

        Files.createDirectories(carpetaEvidencias);

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null ||
                nombreOriginal.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El archivo no tiene un nombre válido."
            );
        }

        nombreOriginal = Paths
                .get(nombreOriginal)
                .getFileName()
                .toString();

        String extension = "";

        int posicion = nombreOriginal.lastIndexOf('.');

        if (posicion >= 0) {
            extension = nombreOriginal.substring(posicion);
        }

        String nombreGuardado =
                UUID.randomUUID() + extension;

        Path destino =
                carpetaEvidencias.resolve(nombreGuardado);

        Files.copy(
                archivo.getInputStream(),
                destino
        );

        String urlAlmacenamiento =
                "/uploads/evidencias/" + nombreGuardado;

        try {

            return adjuntoRepository.agregarAdjunto(
                    idSolicitud,
                    idUsuario,
                    nombreOriginal,
                    tipo,
                    archivo.getSize(),
                    urlAlmacenamiento
            );

        } catch (RuntimeException e) {

            try {
                Files.deleteIfExists(destino);
            } catch (IOException ignored) {
            }

            throw e;
        }
    }
}