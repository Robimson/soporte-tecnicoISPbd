package com.soportenet.soportetecnico.scheduling;

import com.soportenet.soportetecnico.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CierreAutomaticoSchedulerTest {

    private SolicitudRepository solicitudRepository;
    private CierreAutomaticoScheduler scheduler;

    @BeforeEach
    void configurar() {

        solicitudRepository =
                Mockito.mock(SolicitudRepository.class);

        scheduler =
                new CierreAutomaticoScheduler(
                        solicitudRepository
                );
    }

    /**
     * Comprueba que el scheduler invoque
     * el procedimiento de cierre automatico.
     */
    @Test
    void ejecutaCierreAutomatico() {

        when(
                solicitudRepository
                        .cerrarSolicitudesVencidas()
        ).thenReturn(3);

        scheduler.cerrarSolicitudesVencidas();

        verify(
                solicitudRepository
        ).cerrarSolicitudesVencidas();
    }

    /**
     * Tambien debe funcionar cuando
     * no existen solicitudes vencidas.
     */
    @Test
    void funcionaCuandoNoHaySolicitudesParaCerrar() {

        when(
                solicitudRepository
                        .cerrarSolicitudesVencidas()
        ).thenReturn(0);

        scheduler.cerrarSolicitudesVencidas();

        verify(
                solicitudRepository
        ).cerrarSolicitudesVencidas();
    }

    /**
     * La funcion SQL podria devolver null.
     * El scheduler no debe fallar por ello.
     */
    @Test
    void funcionaCuandoProcedimientoDevuelveNull() {

        when(
                solicitudRepository
                        .cerrarSolicitudesVencidas()
        ).thenReturn(null);

        scheduler.cerrarSolicitudesVencidas();

        verify(
                solicitudRepository
        ).cerrarSolicitudesVencidas();
    }
}
