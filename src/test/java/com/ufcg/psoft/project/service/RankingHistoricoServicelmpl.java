package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.dto.ranking.HistoricoRankingResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingSnapshotResponseDTO;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import com.ufcg.psoft.project.service.ranking.RankingCalculator;
import com.ufcg.psoft.project.service.ranking.RankingHistoricoServiceImpl;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes unitarios do RankingHistoricoServiceImpl - US18")
class RankingHistoricoServiceImplTest {

    @Mock private RankingSnapshotRepository rankingSnapshotRepository;
    @Mock private GrupoRepository grupoRepository;
    @Mock private PartidaRepository partidaRepository;
    @Mock private PalpiteRepository palpiteRepository;
    @Mock private PontuacaoService pontuacaoService;
    @Mock private RankingCalculator rankingCalculator;

    @InjectMocks
    private RankingHistoricoServiceImpl rankingHistoricoService;

    private Usuario usuario;
    private Grupo grupo;
    private Partida partida;
    private RankingSnapshot snapshot;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rankingHistoricoService, "desempenhoRecentePartidas", 5);

        usuario = Usuario.builder()
                .id(1L)
                .nome("Usuario Teste")
                .email("teste@email.com")
                .username("teste")
                .endereco("Rua A")
                .codigo("111111")
                .perfil(PerfilUsuario.PADRAO)
                .build();

        Campeonato campeonato = Campeonato.builder()
                .id(1L)
                .nome("Campeonato Teste")
                .url("http://campeonato.com")
                .codigo("CAT001")
                .ativo(true)
                .build();

        grupo = Grupo.builder()
                .id(1L)
                .nome("Grupo Teste")
                .campeonato(campeonato)
                .organizador(usuario)
                .participantes(Set.of(usuario))
                .build();

        partida = Partida.builder()
                .id(1L)
                .campeonato(campeonato)
                .codigoExterno(1L)
                .mandante("Time A")
                .visitante("Time B")
                .golsMandante(2)
                .golsVisitante(1)
                .status(PartidaStatus.FINALIZADO)
                .data(LocalDateTime.now().minusDays(1))
                .build();

        snapshot = RankingSnapshot.builder()
                .id(1L)
                .grupo(grupo)
                .usuario(usuario)
                .partida(partida)
                .posicao(1)
                .pontuacao(10)
                .dataSnapshot(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("obterHistorico")
    class ObterHistorico {

        @Test
        @DisplayName("Deve retornar historico completo do grupo")
        void deveRetornarHistoricoCompleto() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdOrderByDataSnapshotAscPosicaoAsc(1L))
                    .thenReturn(List.of(snapshot));

            HistoricoRankingResponseDTO resultado = rankingHistoricoService.obterHistorico(1L);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1L, resultado.getGrupoId()),
                    () -> assertEquals(1, resultado.getSnapshots().size()),
                    () -> assertEquals(1, resultado.getSnapshots().get(0).getPosicao()),
                    () -> assertEquals(10, resultado.getSnapshots().get(0).getPontuacao())
            );
        }

        @Test
        @DisplayName("Deve retornar historico vazio quando nao ha snapshots")
        void deveRetornarHistoricoVazio() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdOrderByDataSnapshotAscPosicaoAsc(1L))
                    .thenReturn(List.of());

            HistoricoRankingResponseDTO resultado = rankingHistoricoService.obterHistorico(1L);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertTrue(resultado.getSnapshots().isEmpty())
            );
        }

        @Test
        @DisplayName("Deve lancar excecao quando grupo nao existe")
        void deveLancarExcecaoQuandoGrupoNaoExiste() {
            when(grupoRepository.existsById(99L)).thenReturn(false);

            assertThrows(GrupoNaoExisteException.class,
                    () -> rankingHistoricoService.obterHistorico(99L));
        }
    }

    @Nested
    @DisplayName("obterHistoricoPorParticipante")
    class ObterHistoricoPorParticipante {

        @Test
        @DisplayName("Deve retornar historico de um participante especifico")
        void deveRetornarHistoricoDoParticipante() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdAndUsuarioIdOrderByDataSnapshotAsc(1L, 1L))
                    .thenReturn(List.of(snapshot));

            HistoricoRankingResponseDTO resultado = rankingHistoricoService.obterHistoricoPorParticipante(1L, 1L);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.getSnapshots().size()),
                    () -> assertEquals(1L, resultado.getSnapshots().get(0).getUsuarioId())
            );
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando participante nao tem snapshots")
        void deveRetornarListaVaziaQuandoSemSnapshots() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdAndUsuarioIdOrderByDataSnapshotAsc(1L, 99L))
                    .thenReturn(List.of());

            HistoricoRankingResponseDTO resultado = rankingHistoricoService.obterHistoricoPorParticipante(1L, 99L);

            assertTrue(resultado.getSnapshots().isEmpty());
        }

        @Test
        @DisplayName("Deve lancar excecao quando grupo nao existe")
        void deveLancarExcecaoQuandoGrupoNaoExiste() {
            when(grupoRepository.existsById(99L)).thenReturn(false);

            assertThrows(GrupoNaoExisteException.class,
                    () -> rankingHistoricoService.obterHistoricoPorParticipante(99L, 1L));
        }
    }

    @Nested
    @DisplayName("obterLideresHistoricos")
    class ObterLideresHistoricos {

        @Test
        @DisplayName("Deve retornar todos os lideres historicos")
        void deveRetornarLideresHistoricos() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdAndPosicaoOrderByDataSnapshotAsc(1L, 1))
                    .thenReturn(List.of(snapshot));

            List<RankingSnapshotResponseDTO> resultado = rankingHistoricoService.obterLideresHistoricos(1L);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(1, resultado.get(0).getPosicao())
            );
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha lideres")
        void deveRetornarListaVaziaQuandoSemLideres() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdAndPosicaoOrderByDataSnapshotAsc(1L, 1))
                    .thenReturn(List.of());

            List<RankingSnapshotResponseDTO> resultado = rankingHistoricoService.obterLideresHistoricos(1L);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lancar excecao quando grupo nao existe")
        void deveLancarExcecaoQuandoGrupoNaoExiste() {
            when(grupoRepository.existsById(99L)).thenReturn(false);

            assertThrows(GrupoNaoExisteException.class,
                    () -> rankingHistoricoService.obterLideresHistoricos(99L));
        }
    }

    @Nested
    @DisplayName("obterDesempenhoRecente")
    class ObterDesempenhoRecente {

        @Test
        @DisplayName("Deve retornar snapshots das ultimas N partidas")
        void deveRetornarDesempenhoRecente() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdOrderByDataSnapshotDescPosicaoAsc(1L))
                    .thenReturn(List.of(snapshot));

            List<RankingSnapshotResponseDTO> resultado = rankingHistoricoService.obterDesempenhoRecente(1L);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size())
            );
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha snapshots recentes")
        void deveRetornarListaVaziaQuandoSemSnapshots() {
            when(grupoRepository.existsById(1L)).thenReturn(true);
            when(rankingSnapshotRepository.findByGrupoIdOrderByDataSnapshotDescPosicaoAsc(1L))
                    .thenReturn(List.of());

            List<RankingSnapshotResponseDTO> resultado = rankingHistoricoService.obterDesempenhoRecente(1L);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lancar excecao quando grupo nao existe")
        void deveLancarExcecaoQuandoGrupoNaoExiste() {
            when(grupoRepository.existsById(99L)).thenReturn(false);

            assertThrows(GrupoNaoExisteException.class,
                    () -> rankingHistoricoService.obterDesempenhoRecente(99L));
        }
    }

    @Nested
    @DisplayName("gerarSnapshot")
    class GerarSnapshot {

        @Test
        @DisplayName("Deve gerar snapshot quando nao existe para grupo e partida")
        void deveGerarSnapshotQuandoNaoExiste() {
            when(rankingSnapshotRepository.existsByGrupoIdAndPartidaId(1L, 1L)).thenReturn(false);
            when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupo));
            when(partidaRepository.findById(1L)).thenReturn(Optional.of(partida));

            com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO pontuacaoDTO =
                    com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO.builder()
                            .grupoId(1L)
                            .usuarioId(1L)
                            .usuarioNome("Usuario Teste")
                            .pontuacao(10)
                            .build();

            when(pontuacaoService.listarPontuacoesParticipantesDoGrupo(1L, usuario.getId(), usuario.getCodigo()))
                    .thenReturn(List.of(pontuacaoDTO));
            when(rankingCalculator.calcularPosicoes(any(), any()))
                    .thenReturn(Map.of(1L, 1));

            rankingHistoricoService.gerarSnapshot(1L, 1L);

            verify(rankingSnapshotRepository).saveAll(any());
        }

        @Test
        @DisplayName("Nao deve gerar snapshot duplicado para mesma partida e grupo")
        void naoDeveGerarSnapshotDuplicado() {
            when(rankingSnapshotRepository.existsByGrupoIdAndPartidaId(1L, 1L)).thenReturn(true);

            rankingHistoricoService.gerarSnapshot(1L, 1L);

            verify(rankingSnapshotRepository, never()).saveAll(any());
        }
    }
}