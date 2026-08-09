package com.ufcg.psoft.project.model.estadopartida;

import com.ufcg.psoft.project.exception.palpite.PalpiteForaDoTempoException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoFinalizadaException;
import com.ufcg.psoft.project.model.JanelaDePalpites;
import com.ufcg.psoft.project.model.Partida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Comportamentos dos estados de uma partida")
class EstadoPartidaTest {

    private static final LocalDateTime DATA_PARTIDA = LocalDateTime.of(2026, 8, 10, 18, 0);
    private static final LocalDateTime DENTRO_DA_JANELA = DATA_PARTIDA.minusMinutes(60);
    private static final LocalDateTime FORA_DA_JANELA = DATA_PARTIDA.minusMinutes(180);

    private final JanelaDePalpites janela = new JanelaDePalpites(120, 30);
    private Partida partida;

    @BeforeEach
    void setUp() {
        partida = Partida.builder()
                .data(DATA_PARTIDA)
                .build();
    }

    @Nested
    @DisplayName("Estado aberto")
    class EstadoAberto {

        private final EstadoPartida estado = new EstadoPartidaAberta();

        @Test
        void permiteOperacoesDePalpiteDentroDaJanela() {
            assertTrue(estado.estaAbertaParaPalpite(partida, janela, DENTRO_DA_JANELA));
            assertDoesNotThrow(() -> estado.validarCriacaoPalpite(partida, janela, DENTRO_DA_JANELA));
            assertDoesNotThrow(() -> estado.validarEdicaoPalpite(partida, janela, DENTRO_DA_JANELA));
            assertDoesNotThrow(() -> estado.validarExclusaoPalpite(partida, janela, DENTRO_DA_JANELA));
        }

        @Test
        void bloqueiaOperacoesDePalpiteForaDaJanela() {
            assertFalse(estado.estaAbertaParaPalpite(partida, janela, FORA_DA_JANELA));
            assertThrows(PalpiteForaDoTempoException.class,
                    () -> estado.validarCriacaoPalpite(partida, janela, FORA_DA_JANELA));
            assertThrows(PalpiteForaDoTempoException.class,
                    () -> estado.validarEdicaoPalpite(partida, janela, FORA_DA_JANELA));
            assertThrows(PalpiteForaDoTempoException.class,
                    () -> estado.validarExclusaoPalpite(partida, janela, FORA_DA_JANELA));
        }

        @Test
        void bloqueiaConsolidacao() {
            assertThrows(PartidaNaoFinalizadaException.class, estado::validarConsolidacao);
        }
    }

    @Nested
    @DisplayName("Estado em andamento")
    class EstadoEmAndamento {

        private final EstadoPartida estado = new EstadoPartidaEmAndamento();

        @Test
        void bloqueiaOperacoesDePalpiteEConsolidacao() {
            assertFalse(estado.estaAbertaParaPalpite(partida, janela, DENTRO_DA_JANELA));
            assertOperacoesBloqueadas(estado);
            assertThrows(PartidaNaoFinalizadaException.class, estado::validarConsolidacao);
        }
    }

    @Nested
    @DisplayName("Estado finalizado")
    class EstadoFinalizado {

        private final EstadoPartida estado = new EstadoPartidaFinalizada();

        @Test
        void bloqueiaPalpitesMasPermiteConsolidacao() {
            assertFalse(estado.estaAbertaParaPalpite(partida, janela, DENTRO_DA_JANELA));
            assertOperacoesBloqueadas(estado);
            assertDoesNotThrow(estado::validarConsolidacao);
        }
    }

    @Nested
    @DisplayName("Estado cancelado")
    class EstadoCancelado {

        private final EstadoPartida estado = new EstadoPartidaCancelada();

        @Test
        void bloqueiaOperacoesDePalpiteEConsolidacao() {
            assertFalse(estado.estaAbertaParaPalpite(partida, janela, DENTRO_DA_JANELA));
            assertOperacoesBloqueadas(estado);
            assertThrows(PartidaNaoFinalizadaException.class, estado::validarConsolidacao);
        }
    }

    private void assertOperacoesBloqueadas(EstadoPartida estado) {
        assertThrows(PalpiteForaDoTempoException.class,
                () -> estado.validarCriacaoPalpite(partida, janela, DENTRO_DA_JANELA));
        assertThrows(PalpiteForaDoTempoException.class,
                () -> estado.validarEdicaoPalpite(partida, janela, DENTRO_DA_JANELA));
        assertThrows(PalpiteForaDoTempoException.class,
                () -> estado.validarExclusaoPalpite(partida, janela, DENTRO_DA_JANELA));
    }
}
