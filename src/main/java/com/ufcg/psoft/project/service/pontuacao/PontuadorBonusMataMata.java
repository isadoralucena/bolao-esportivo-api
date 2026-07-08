package com.ufcg.psoft.project.service.pontuacao;

import org.springframework.stereotype.Component;

import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

@Component
public class PontuadorBonusMataMata implements Pontuador {

    @Override
    public TipoRegraPontuacao getTipo() {
        return TipoRegraPontuacao.BONUS_MATA_MATA;
    }


    @Override
    public int calcular(PontuacaoPalpite pontuacaoPalpite, RegraPontuacao regra) {
        Partida partida = pontuacaoPalpite.getPalpite().getPartida();

        if (partida.isMataMata() && pontuacaoPalpite.isAcertouAlgo()) {
            return regra.getPontos();
        }

        return 0;
    }
}