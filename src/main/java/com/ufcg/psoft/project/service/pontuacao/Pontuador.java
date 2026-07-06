package com.ufcg.psoft.project.service.pontuacao;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

public interface Pontuador {

    TipoRegraPontuacao getTipo();

    int calcular(Partida partida, Palpite palpite, int pontos);
}