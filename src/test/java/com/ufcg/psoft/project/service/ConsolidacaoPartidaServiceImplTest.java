package com.ufcg.psoft.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufcg.psoft.project.event.PartidaFinalizadaEvent;
import com.ufcg.psoft.project.exception.partida.PartidaSyncException;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.consolidacao.ConsolidacaoPartidaServiceImpl;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do serviço de consolidação de partidas")
class ConsolidacaoPartidaServiceImplTest {

    @Mock
    private PontuacaoService pontuacaoService;

    @Mock
    private PartidaRepository partidaRepository;

    @InjectMocks
    private ConsolidacaoPartidaServiceImpl consolidacaoService;

    private Partida partida;

    @BeforeEach
    void setUp() {
        partida = Partida.builder()
                .id(1L)
                .status(PartidaStatus.FINALIZADO)
                .golsMandante(2)
                .golsVisitante(1)
                .consolidada(false)
                .build();
    }

    @Nested
    @DisplayName("Consolidação")
    class Consolidacao {

        @Test
        @DisplayName("Não consolida partida que ainda não foi finalizada")
        void quandoPartidaNaoFinalizadaNaoConsolida() {
            partida.setStatus(PartidaStatus.EM_ANDAMENTO);

            consolidacaoService.consolidar(partida);

            assertFalse(partida.isConsolidada());
            verifyNoInteractions(pontuacaoService, partidaRepository);
        }

        @Test
        @DisplayName("Não consolida novamente uma partida já consolidada")
        void quandoPartidaJaConsolidadaNaoConsolidaNovamente() {
            partida.setConsolidada(true);

            consolidacaoService.consolidar(partida);

            assertTrue(partida.isConsolidada());
            verifyNoInteractions(pontuacaoService, partidaRepository);
        }

        @Test
        @DisplayName("Lança exceção quando gols do mandante são nulos")
        void quandoGolsMandanteNulosLancaExcecao() {
            partida.setGolsMandante(null);

            assertResultadoInvalido(partida);
        }

        @Test
        @DisplayName("Lança exceção quando gols do mandante são negativos")
        void quandoGolsMandanteNegativosLancaExcecao() {
            partida.setGolsMandante(-1);

            assertResultadoInvalido(partida);
        }

        @Test
        @DisplayName("Lança exceção quando gols do visitante são nulos")
        void quandoGolsVisitanteNulosLancaExcecao() {
            partida.setGolsVisitante(null);

            assertResultadoInvalido(partida);
        }

        @Test
        @DisplayName("Lança exceção quando gols do visitante são negativos")
        void quandoGolsVisitanteNegativosLancaExcecao() {
            partida.setGolsVisitante(-1);

            assertResultadoInvalido(partida);
        }

        @Test
        @DisplayName("Calcula pontuações, marca e salva uma partida válida")
        void quandoPartidaValidaConsolida() {
            consolidacaoService.consolidar(partida);

            assertTrue(partida.isConsolidada());

            verify(pontuacaoService)
                    .calcularPontuacoesAssociadasAPartida(1L);

            verify(partidaRepository).save(partida);
        }
    }

    @Nested
    @DisplayName("Evento de partida finalizada")
    class EventoPartidaFinalizada {

        @Test
        @DisplayName("Consolida a partida recebida pelo evento")
        void quandoRecebeEventoConsolidaPartida() {
            PartidaFinalizadaEvent event =
                    new PartidaFinalizadaEvent(this, partida);

            consolidacaoService.aoFinalizarPartida(event);

            assertTrue(partida.isConsolidada());

            verify(pontuacaoService)
                    .calcularPontuacoesAssociadasAPartida(partida.getId());

            verify(partidaRepository).save(partida);
        }

        @Test
        @DisplayName("Não processa evento de partida já consolidada")
        void quandoEventoPossuiPartidaJaConsolidadaNaoProcessa() {
            partida.setConsolidada(true);

            PartidaFinalizadaEvent event =
                    new PartidaFinalizadaEvent(this, partida);

            consolidacaoService.aoFinalizarPartida(event);

            verify(pontuacaoService, never())
                    .calcularPontuacoesAssociadasAPartida(partida.getId());

            verify(partidaRepository, never()).save(partida);
        }
    }

    private void assertResultadoInvalido(Partida partidaInvalida) {
        PartidaSyncException exception = assertThrows(
                PartidaSyncException.class,
                () -> consolidacaoService.consolidar(partidaInvalida)
        );

        assertEquals(
                "Resultado inválido para consolidação da partida 1",
                exception.getMessage()
        );

        assertFalse(partidaInvalida.isConsolidada());

        verifyNoInteractions(pontuacaoService, partidaRepository);
    }
}