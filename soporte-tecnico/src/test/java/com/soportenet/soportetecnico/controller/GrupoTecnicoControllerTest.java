package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.AgregarMiembroRequest;
import com.soportenet.soportetecnico.dto.CrearGrupoRequest;
import com.soportenet.soportetecnico.entity.GrupoTecnico;
import com.soportenet.soportetecnico.repository.GrupoTecnicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GrupoTecnicoControllerTest {

    private GrupoTecnicoRepository grupoTecnicoRepository;
    private GrupoTecnicoController controller;

    @BeforeEach
    void configurar() {
        grupoTecnicoRepository = Mockito.mock(GrupoTecnicoRepository.class);
        controller = new GrupoTecnicoController(grupoTecnicoRepository);
    }

    @Test
    void crearGrupoDevuelveCreated() {

        CrearGrupoRequest request = new CrearGrupoRequest();
        request.setNombreGrupo("Fibra Optica");

        GrupoTecnico guardado =
                new GrupoTecnico(1L, "Fibra Optica");

        when(grupoTecnicoRepository.save(any(GrupoTecnico.class)))
                .thenReturn(guardado);

        ResponseEntity<GrupoTecnico> respuesta =
                controller.crear(request);

        assertEquals(
                HttpStatus.CREATED,
                respuesta.getStatusCode()
        );

        assertEquals(
                "Fibra Optica",
                respuesta.getBody().getNombreGrupo()
        );

        verify(grupoTecnicoRepository)
                .save(any(GrupoTecnico.class));
    }

    @Test
    void listarDevuelveTodosLosGrupos() {

        GrupoTecnico grupo1 =
                new GrupoTecnico(1L, "Fibra Optica");

        GrupoTecnico grupo2 =
                new GrupoTecnico(2L, "Radio Enlace");

        when(grupoTecnicoRepository.findAll())
                .thenReturn(List.of(grupo1, grupo2));

        List<GrupoTecnico> respuesta =
                controller.listar();

        assertEquals(
                2,
                respuesta.size()
        );

        verify(grupoTecnicoRepository)
                .findAll();
    }

    @Test
    void agregarMiembroDevuelveCreated() {

        Long idGrupo = 10L;
        Long idTecnico = 50L;

        AgregarMiembroRequest request =
                new AgregarMiembroRequest();

        request.setIdTecnico(idTecnico);

        ResponseEntity<Void> respuesta =
                controller.agregarMiembro(
                        idGrupo,
                        request
                );

        assertEquals(
                HttpStatus.CREATED,
                respuesta.getStatusCode()
        );

        verify(grupoTecnicoRepository)
                .agregarMiembro(
                        idTecnico,
                        idGrupo
                );
    }

    @Test
    void retirarMiembroDevuelveNoContent() {

        Long idGrupo = 10L;
        Long idTecnico = 50L;

        ResponseEntity<Void> respuesta =
                controller.retirarMiembro(
                        idGrupo,
                        idTecnico
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                respuesta.getStatusCode()
        );

        verify(grupoTecnicoRepository)
                .retirarMiembro(
                        idTecnico,
                        idGrupo
                );
    }
}
