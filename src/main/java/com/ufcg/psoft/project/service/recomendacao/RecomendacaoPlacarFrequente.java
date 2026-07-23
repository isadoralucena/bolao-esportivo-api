package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("PLACAR_FREQUENTE")
public class RecomendacaoPlacarFrequente extends RecomendacaoStrategyBase {

    private record Placar(int mandante, int visitante) {}

    @Override
    public String getNome() {
        return "PLACAR_FREQUENTE";
    }

    @Override
    public RecomendacaoResponseDTO recomendar(Partida partida) {
        List<Partida> finalizadas = buscarPartidasFinalizadas(partida.getCampeonato().getId());

        if (finalizadas.isEmpty()) {
            return semHistorico();
        }

        Placar placar = encontrarPlacarMaisFrequente(finalizadas);

        return RecomendacaoResponseDTO.builder()
                .golsMandanteRecomendado(placar.mandante())
                .golsVisitanteRecomendado(placar.visitante())
                .estrategia(getNome())
                .temHistorico(true)
                .build();
    }

    private Placar encontrarPlacarMaisFrequente(List<Partida> finalizadas) {
        return finalizadas.stream()
                .collect(Collectors.groupingBy(
                        p -> new Placar(p.getGolsMandante(), p.getGolsVisitante()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(new Placar(0, 0));
    }
}