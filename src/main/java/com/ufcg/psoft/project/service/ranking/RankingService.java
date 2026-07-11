package com.ufcg.psoft.project.service.ranking;

import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;

public interface RankingService {
    RankingResponseDTO rankingDoGrupo(Long grupoId, Long usuarioId, String codigoAcesso);
    RankingResponseDTO rankingGlobal(Long usuarioId, String codigoAcesso);
}
