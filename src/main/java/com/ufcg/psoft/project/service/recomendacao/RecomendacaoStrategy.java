package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;

public interface RecomendacaoStrategy {
    String getNome();
    RecomendacaoResponseDTO recomendar(Partida partida);
}