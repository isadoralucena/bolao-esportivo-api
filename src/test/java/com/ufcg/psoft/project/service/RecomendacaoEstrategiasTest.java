package com.ufcg.psoft.project.service;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.recomendacao.RecomendacaoMediaGols;
import com.ufcg.psoft.project.service.recomendacao.RecomendacaoPlacarFrequente;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes unitarios das estrategias de recomendacao - US20")
class RecomendacaoEstrategiasTest {

    @Mock
    private PartidaRepository partidaRepository;

    @InjectMocks
    private RecomendacaoPlacarFrequente placarFrequente;

    @InjectMocks
    private RecomendacaoMediaGols mediaGols;

    private Campeonato campeonato;
    private Partida partida;

    @BeforeEach
    void setUp() {
        campeonato = Campeonato.builder()
                .id(1L).nome("Campeonato Teste")
                .url("http://campeonato.com")
                .codigo("CAT001").ativo(true).build();

        partida = Partida.builder()
                .id(1L).campeonato(campeonato)
                .codigoExterno(1L).mandante("Time A")
                .visitante("Time B").status(PartidaStatus.ABERTO)
                .data(LocalDateTime.now(FIXED_CLOCK).plusDays(1)).build();

        ReflectionTestUtils.setField(placarFrequente, "partidaRepository", partidaRepository);
        ReflectionTestUtils.setField(mediaGols, "partidaRepository", partidaRepository);
    }

    private Partida criarPartidaFinalizada(Long id, Long codigoExterno, int golsMandante, int golsVisitante) {
        return Partida.builder()
                .id(id).campeonato(campeonato)
                .codigoExterno(codigoExterno)
                .mandante("Time X").visitante("Time Y")
                .golsMandante(golsMandante).golsVisitante(golsVisitante)
                .status(PartidaStatus.FINALIZADO)
                .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1)).build();
    }

    @Nested
    @DisplayName("RecomendacaoPlacarFrequente")
    class PlacarFrequenteTests {

        @Test
        @DisplayName("Deve retornar Optional vazio quando nao ha partidas finalizadas")
        void deveRetornarVazioSemHistorico() {
            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(List.of());

            Optional<RecomendacaoResponseDTO> resultado = placarFrequente.recomendar(partida);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar placar mais frequente quando ha historico")
        void deveRetornarPlacarMaisFrequente() {
            List<Partida> finalizadas = List.of(
                    criarPartidaFinalizada(2L, 2L, 2, 1),
                    criarPartidaFinalizada(3L, 3L, 2, 1),
                    criarPartidaFinalizada(4L, 4L, 1, 0)
            );
            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(finalizadas);

            Optional<RecomendacaoResponseDTO> resultado = placarFrequente.recomendar(partida);

            assertAll(
                    () -> assertTrue(resultado.isPresent()),
                    () -> assertEquals(2, resultado.get().getGolsMandanteRecomendado()),
                    () -> assertEquals(1, resultado.get().getGolsVisitanteRecomendado()),
                    () -> assertEquals("PLACAR_FREQUENTE", resultado.get().getEstrategia()),
                    () -> assertTrue(resultado.get().isTemRecomendacao()),
                    () -> assertNotNull(resultado.get().getMensagem())
            );
        }

        @Test
        @DisplayName("Deve permitir a estrategia de media quando nenhum placar se repete")
        void deveRetornarVazioSemPlacarPredominante() {
            List<Partida> finalizadas = List.of(
                    criarPartidaFinalizada(2L, 2L, 2, 1),
                    criarPartidaFinalizada(3L, 3L, 1, 0)
            );
            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(finalizadas);

            assertTrue(placarFrequente.recomendar(partida).isEmpty());
            assertTrue(mediaGols.recomendar(partida).isPresent());
        }

        @Test
        @DisplayName("Deve ignorar partidas sem gols definidos")
        void deveIgnorarPartidasSemGols() {
            Partida semGols = Partida.builder()
                    .id(5L).campeonato(campeonato)
                    .codigoExterno(5L).status(PartidaStatus.FINALIZADO)
                    .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1)).build();

            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(List.of(semGols));

            Optional<RecomendacaoResponseDTO> resultado = placarFrequente.recomendar(partida);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar getNome correto")
        void deveRetornarNomeCorreto() {
            assertEquals("PLACAR_FREQUENTE", placarFrequente.getNome());
        }
    }

    @Nested
    @DisplayName("RecomendacaoMediaGols")
    class MediaGolsTests {

        @Test
        @DisplayName("Deve retornar Optional vazio quando nao ha partidas finalizadas")
        void deveRetornarVazioSemHistorico() {
            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(List.of());

            Optional<RecomendacaoResponseDTO> resultado = mediaGols.recomendar(partida);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar media arredondada quando ha historico")
        void deveRetornarMediaArredondada() {
            List<Partida> finalizadas = List.of(
                    criarPartidaFinalizada(2L, 2L, 2, 1),
                    criarPartidaFinalizada(3L, 3L, 1, 0),
                    criarPartidaFinalizada(4L, 4L, 2, 1)
            );
            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(finalizadas);

            Optional<RecomendacaoResponseDTO> resultado = mediaGols.recomendar(partida);

            assertAll(
                    () -> assertTrue(resultado.isPresent()),
                    () -> assertEquals(2, resultado.get().getGolsMandanteRecomendado()),
                    () -> assertEquals(1, resultado.get().getGolsVisitanteRecomendado()),
                    () -> assertEquals("MEDIA_GOLS", resultado.get().getEstrategia()),
                    () -> assertTrue(resultado.get().isTemRecomendacao()),
                    () -> assertNotNull(resultado.get().getMensagem())
            );
        }

        @Test
        @DisplayName("Deve arredondar media corretamente")
        void deveArredondarMediaCorretamente() {
            List<Partida> finalizadas = List.of(
                    criarPartidaFinalizada(2L, 2L, 1, 0),
                    criarPartidaFinalizada(3L, 3L, 2, 1)
            );
            when(partidaRepository.findByCampeonatoId(1L)).thenReturn(finalizadas);

            Optional<RecomendacaoResponseDTO> resultado = mediaGols.recomendar(partida);

            assertAll(
                    () -> assertTrue(resultado.isPresent()),
                    () -> assertEquals(2, resultado.get().getGolsMandanteRecomendado()),
                    () -> assertEquals(1, resultado.get().getGolsVisitanteRecomendado())
            );
        }

        @Test
        @DisplayName("Deve retornar getNome correto")
        void deveRetornarNomeCorreto() {
            assertEquals("MEDIA_GOLS", mediaGols.getNome());
        }
    }
}
