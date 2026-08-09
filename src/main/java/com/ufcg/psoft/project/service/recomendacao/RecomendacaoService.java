package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;
import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;

import java.util.List;

public interface RecomendacaoService {
    RecomendacaoResponseDTO recomendar(Long grupoId, Long partidaId, Long usuarioId, String codigo);
    RecomendacaoResponseDTO recomendar(Long partidaId, Long usuarioId, String codigo);
    List<PartidaResponseDTO> listarPartidasFuturasComRecomendacao(Long usuarioId, String codigo);
}