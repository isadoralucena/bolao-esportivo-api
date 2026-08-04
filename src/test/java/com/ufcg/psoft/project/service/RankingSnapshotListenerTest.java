package com.ufcg.psoft.project.service;

import static com.ufcg.psoft.project.config.TestClockConfig.FIXED_CLOCK;

import com.ufcg.psoft.project.event.PartidaFinalizadaEvent;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.service.ranking.RankingHistoricoService;
import com.ufcg.psoft.project.service.ranking.RankingSnapshotListener;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes unitarios do RankingSnapshotListener - US18")
class RankingSnapshotListenerTest {

    @Mock
    private RankingHistoricoService rankingHistoricoService;

    @Mock
    private PalpiteRepository palpiteRepository;

    @InjectMocks
    private RankingSnapshotListener rankingSnapshotListener;

    private Partida partida;
    private Grupo grupo;
    private Palpite palpite;

    @BeforeEach
    void setUp() {
        Campeonato campeonato = Campeonato.builder()
                .id(1L).nome("Campeonato Teste")
                .url("http://campeonato.com")
                .codigo("CAT001").ativo(true).build();

        grupo = Grupo.builder()
                .id(1L).nome("Grupo Teste")
                .campeonato(campeonato).build();

        partida = Partida.builder()
                .id(1L).campeonato(campeonato)
                .codigoExterno(1L).mandante("Time A")
                .visitante("Time B").status(PartidaStatus.FINALIZADO)
                .golsMandante(2).golsVisitante(1)
                .data(LocalDateTime.now(FIXED_CLOCK).minusDays(1)).build();

        Usuario usuario = Usuario.builder()
                .id(1L).nome("Usuario Teste")
                .email("teste@email.com").username("teste")
                .endereco("Rua A").codigo("111111").build();

        palpite = Palpite.builder()
                .id(1L).partida(partida).grupo(grupo)
                .usuario(usuario).golsMandante(2).golsVisitante(1)
                .data(LocalDateTime.now(FIXED_CLOCK)).build();
    }

    @Nested
    @DisplayName("aoFinalizarPartida")
    class AoFinalizarPartida {

        @Test
        @DisplayName("Deve gerar snapshot para cada grupo afetado pela partida")
        void deveGerarSnapshotParaCadaGrupoAfetado() {
            PartidaFinalizadaEvent event = new PartidaFinalizadaEvent(this, partida);
            when(palpiteRepository.findByPartidaId(1L)).thenReturn(List.of(palpite));

            rankingSnapshotListener.aoFinalizarPartida(event);

            verify(rankingHistoricoService).gerarSnapshot(1L, 1L);
        }

        @Test
        @DisplayName("Nao deve gerar snapshot quando nao ha palpites para a partida")
        void naoDeveGerarSnapshotSemPalpites() {
            PartidaFinalizadaEvent event = new PartidaFinalizadaEvent(this, partida);
            when(palpiteRepository.findByPartidaId(1L)).thenReturn(List.of());

            rankingSnapshotListener.aoFinalizarPartida(event);

            verify(rankingHistoricoService, never()).gerarSnapshot(any(), any());
        }

        @Test
        @DisplayName("Deve gerar snapshot apenas uma vez por grupo mesmo com multiplos palpites")
        void deveGerarSnapshotUmaVezPorGrupo() {
            Usuario usuario2 = Usuario.builder()
                    .id(2L).nome("Usuario 2")
                    .email("teste2@email.com").username("teste2")
                    .endereco("Rua B").codigo("222222").build();

            Palpite palpite2 = Palpite.builder()
                    .id(2L).partida(partida).grupo(grupo)
                    .usuario(usuario2).golsMandante(1).golsVisitante(0)
                    .data(LocalDateTime.now(FIXED_CLOCK)).build();

            PartidaFinalizadaEvent event = new PartidaFinalizadaEvent(this, partida);
            when(palpiteRepository.findByPartidaId(1L)).thenReturn(List.of(palpite, palpite2));

            rankingSnapshotListener.aoFinalizarPartida(event);

            verify(rankingHistoricoService, times(1)).gerarSnapshot(1L, 1L);
        }
    }
}
