package com.ufcg.psoft.project.service.ranking;

import com.ufcg.psoft.project.dto.ranking.HistoricoRankingResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingSnapshotResponseDTO;

import java.util.List;

public interface RankingHistoricoService {
    HistoricoRankingResponseDTO obterHistorico(Long grupoId);
    HistoricoRankingResponseDTO obterHistoricoPorParticipante(Long grupoId, Long usuarioId);
    List<RankingSnapshotResponseDTO> obterLideresHistoricos(Long grupoId);
    List<RankingSnapshotResponseDTO> obterDesempenhoRecente(Long grupoId);
    void gerarSnapshot(Long grupoId, Long partidaId);
}