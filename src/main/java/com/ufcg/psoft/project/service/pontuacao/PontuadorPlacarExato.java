package com.ufcg.psoft.project.service.pontuacao;

import org.springframework.stereotype.Component;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

@Component
public class PontuadorPlacarExato implements Pontuador {

    @Override
    public TipoRegraPontuacao getTipo() {
        return TipoRegraPontuacao.PLACAR_EXATO;
    }

    @Override
    public int calcular(Partida partida, Palpite palpite, int pontos) {
        boolean acertou = partida.getGolsMandante().equals(palpite.getGolsMandante()) && partida.getGolsVisitante().equals(palpite.getGolsVisitante());

        if (acertou) {
            return pontos;
        }

        return 0;
    }
}