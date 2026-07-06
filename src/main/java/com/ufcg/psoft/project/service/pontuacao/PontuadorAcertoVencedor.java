package com.ufcg.psoft.project.service.pontuacao;

import org.springframework.stereotype.Component;

import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

@Component
public class PontuadorAcertoVencedor implements Pontuador {

    @Override
    public TipoRegraPontuacao getTipo() {
        return TipoRegraPontuacao.ACERTO_VENCEDOR;
    }

    @Override
    public int calcular(Partida partida, Palpite palpite, int pontos) {
        int resultadoReal = Integer.compare(
                partida.getGolsMandante(),
                partida.getGolsVisitante()
        );

        int resultadoPalpite = Integer.compare(
                palpite.getGolsMandante(),
                palpite.getGolsVisitante()
        );

        if (resultadoReal != 0 && resultadoReal == resultadoPalpite) {
            return pontos;
        }

        return 0;
    }
}