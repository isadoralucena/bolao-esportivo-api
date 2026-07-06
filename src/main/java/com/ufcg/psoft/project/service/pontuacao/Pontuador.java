package com.ufcg.psoft.project.service.pontuacao;

import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

public interface Pontuador {
    TipoRegraPontuacao getTipo();
    int calcular(PontuacaoPalpite pontuacaoPalpite, RegraPontuacao regra);
}