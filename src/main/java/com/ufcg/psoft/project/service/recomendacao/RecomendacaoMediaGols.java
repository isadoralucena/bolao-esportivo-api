package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("MEDIA_GOLS")
public class RecomendacaoMediaGols extends RecomendacaoStrategyBase {

    @Override
    public String getNome() {
        return "MEDIA_GOLS";
    }

    @Override
    public RecomendacaoResponseDTO recomendar(Partida partida) {
        List<Partida> finalizadas = buscarPartidasFinalizadas(partida.getCampeonato().getId());

        if (finalizadas.isEmpty()) {
            return semHistorico();
        }

        int mediaMandante = (int) Math.round(
                finalizadas.stream()
                        .mapToInt(Partida::getGolsMandante)
                        .average()
                        .orElse(0)
        );

        int mediaVisitante = (int) Math.round(
                finalizadas.stream()
                        .mapToInt(Partida::getGolsVisitante)
                        .average()
                        .orElse(0)
        );

        return RecomendacaoResponseDTO.builder()
                .golsMandanteRecomendado(mediaMandante)
                .golsVisitanteRecomendado(mediaVisitante)
                .estrategia(getNome())
                .temHistorico(true)
                .build();
    }
}