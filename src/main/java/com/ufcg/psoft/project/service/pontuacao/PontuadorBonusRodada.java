package com.ufcg.psoft.project.service.pontuacao;

import org.springframework.stereotype.Component;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

@Component
public class PontuadorBonusRodada implements Pontuador {

    @Override
    public TipoRegraPontuacao getTipo() {
        return TipoRegraPontuacao.BONUS_RODADA;
    }

    @Override
    public int calcular(Partida partida, Palpite palpite, int pontos) {
        return 0; // TODO
    }
}