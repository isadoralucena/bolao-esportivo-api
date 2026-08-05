package com.ufcg.psoft.project.service;

import com.ufcg.psoft.project.event.RankingAtualizadoEvent;
import com.ufcg.psoft.project.service.ranking.RankingHistoricoService;
import com.ufcg.psoft.project.service.ranking.RankingSnapshotListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do RankingSnapshotListener - US18")
class RankingSnapshotListenerTest {

    @Mock
    private RankingHistoricoService rankingHistoricoService;

    @InjectMocks
    private RankingSnapshotListener rankingSnapshotListener;

    @Test
    @DisplayName("Gera snapshot após atualização vinculada a uma partida")
    void deveGerarSnapshotParaAtualizacaoDePartida() {
        rankingSnapshotListener.aoAtualizarRanking(new RankingAtualizadoEvent(this, 1L, 2L));

        verify(rankingHistoricoService).gerarSnapshot(1L, 2L);
    }

    @Test
    @DisplayName("Gera snapshot sem partida após recálculo do grupo")
    void deveGerarSnapshotParaRecalculoDoGrupo() {
        rankingSnapshotListener.aoAtualizarRanking(new RankingAtualizadoEvent(this, 1L));

        verify(rankingHistoricoService).gerarSnapshot(1L, null);
    }
}
