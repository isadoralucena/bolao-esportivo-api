package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;

public interface RecomendacaoService {
    RecomendacaoResponseDTO recomendar(Long grupoId, Long partidaId, Long usuarioId, String codigo, String estrategia);
}