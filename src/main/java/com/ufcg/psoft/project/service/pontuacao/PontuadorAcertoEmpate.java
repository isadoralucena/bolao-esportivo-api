package com.ufcg.psoft.project.service.pontuacao;

import org.springframework.stereotype.Component;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

@Component
public class PontuadorAcertoEmpate implements Pontuador {

    @Override
    public TipoRegraPontuacao getTipo() {
        return TipoRegraPontuacao.ACERTO_EMPATE;
    }

    @Override
    public int calcular(Partida partida, Palpite palpite, int pontos) {
        boolean partidaEmpatada = partida.getGolsMandante().equals(partida.getGolsVisitante());

        boolean palpiteEmpatado = palpite.getGolsMandante().equals(palpite.getGolsVisitante());

        if (partidaEmpatada && palpiteEmpatado) {
            return pontos;
        }

        return 0;
    }
}