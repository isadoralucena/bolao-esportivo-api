package com.ufcg.psoft.project.service.pontuacao;

import org.springframework.stereotype.Component;

import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

@Component
public class PontuadorPlacarExato implements Pontuador {

    @Override
    public TipoRegraPontuacao getTipo() {
        return TipoRegraPontuacao.PLACAR_EXATO;
    }

    @Override
    public int calcular(PontuacaoPalpite pontuacaoPalpite, RegraPontuacao regra) {
        if (Boolean.TRUE.equals(pontuacaoPalpite.isAcertouPlacarExato())) {
            return regra.getPontos();
        }

        return 0;
    }
}