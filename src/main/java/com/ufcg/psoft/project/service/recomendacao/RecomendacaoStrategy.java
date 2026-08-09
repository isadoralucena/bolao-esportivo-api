package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;

import java.util.Optional;

public interface RecomendacaoStrategy {
    String getNome();
    Optional<RecomendacaoResponseDTO> recomendar(Partida partida);
}