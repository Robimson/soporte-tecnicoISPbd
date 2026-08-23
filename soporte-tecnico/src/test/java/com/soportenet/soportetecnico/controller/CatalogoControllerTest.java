package com.soportenet.soportetecnico.controller;

import com.soportenet.soportetecnico.dto.TecnicoResponse;
import com.soportenet.soportetecnico.entity.Categoria;
import com.soportenet.soportetecnico.entity.Estado;
import com.soportenet.soportetecnico.entity.Prioridad;
import com.soportenet.soportetecnico.entity.Tecnico;
import com.soportenet.soportetecnico.repository.CategoriaRepository;
import com.soportenet.soportetecnico.repository.EstadoRepository;
import com.soportenet.soportetecnico.repository.PrioridadRepository;
import com.soportenet.soportetecnico.repository.TecnicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CatalogoControllerTest {

    private CategoriaRepository categoriaRepository;
    private PrioridadRepository prioridadRepository;
    private EstadoRepository estadoRepository;
    private TecnicoRepository tecnicoRepository;
    private CatalogoController controller;

    @BeforeEach
    void configurar() {

        categoriaRepository =
                Mockito.mock(CategoriaRepository.class);

        prioridadRepository =
                Mockito.mock(PrioridadRepository.class);

        estadoRepository =
                Mockito.mock(EstadoRepository.class);

        tecnicoRepository =
                Mockito.mock(TecnicoRepository.class);

        controller = new CatalogoController(
                categoriaRepository,
                prioridadRepository,
                estadoRepository,
                tecnicoRepository
        );
    }

    @Test
    void listarCategoriasDevuelveTodas() {

        Categoria categoria1 = new Categoria();
        Categoria categoria2 = new Categoria();

        when(categoriaRepository.findAll())
                .thenReturn(
                        List.of(
                                categoria1,
                                categoria2
                        )
                );

        List<Categoria> respuesta =
                controller.listarCategorias();

        assertEquals(
                2,
                respuesta.size()
        );

        verify(categoriaRepository)
                .findAll();
    }

    @Test
    void listarPrioridadesDevuelveTodas() {

        Prioridad prioridad1 = new Prioridad();
        Prioridad prioridad2 = new Prioridad();

        when(prioridadRepository.findAll())
                .thenReturn(
                        List.of(
                                prioridad1,
                                prioridad2
                        )
                );

        List<Prioridad> respuesta =
                controller.listarPrioridades();

        assertEquals(
                2,
                respuesta.size()
        );

        verify(prioridadRepository)
                .findAll();
    }

    @Test
    void listarEstadosDevuelveTodos() {

        Estado estado1 = new Estado();
        Estado estado2 = new Estado();

        when(estadoRepository.findAll())
                .thenReturn(
                        List.of(
                                estado1,
                                estado2
                        )
                );

        List<Estado> respuesta =
                controller.listarEstados();

        assertEquals(
                2,
                respuesta.size()
        );

        verify(estadoRepository)
                .findAll();
    }

    @Test
    void listarTecnicosPorDefectoBuscaHabilitados() {

        Tecnico tecnico = new Tecnico();

        when(tecnicoRepository.findByHabilitado(true))
                .thenReturn(List.of(tecnico));

        /*
         * true representa el valor por defecto
         * del endpoint /api/tecnicos.
         */
        List<TecnicoResponse> respuesta =
                controller.listarTecnicos(true);

        assertEquals(
                1,
                respuesta.size()
        );

        verify(tecnicoRepository)
                .findByHabilitado(true);
    }

    @Test
    void listarTecnicosPuedeBuscarDeshabilitados() {

        when(tecnicoRepository.findByHabilitado(false))
                .thenReturn(List.of());

        List<TecnicoResponse> respuesta =
                controller.listarTecnicos(false);

        assertEquals(
                0,
                respuesta.size()
        );

        verify(tecnicoRepository)
                .findByHabilitado(false);
    }
}
