package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.repository.PartidaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("PLACAR_FREQUENTE")
public class RecomendacaoPlacarFrequente extends RecomendacaoStrategyBase {

    public RecomendacaoPlacarFrequente(PartidaRepository partidaRepository) {
        super(partidaRepository);
    }

    private record Placar(int mandante, int visitante) {}

    @Override
    public String getNome() {
        return "PLACAR_FREQUENTE";
    }

    @Override
    public Optional<RecomendacaoResponseDTO> recomendar(Partida partida) {
        List<Partida> finalizadas = buscarPartidasFinalizadas(partida.getCampeonato().getId());

        if (finalizadas.isEmpty()) {
            return semHistorico();
        }

        Optional<Placar> placarFrequente = encontrarPlacarMaisFrequente(finalizadas);
        if (placarFrequente.isEmpty()) {
            return Optional.empty();
        }

        Placar placar = placarFrequente.get();

        return Optional.of(RecomendacaoResponseDTO.builder()
                .golsMandanteRecomendado(placar.mandante())
                .golsVisitanteRecomendado(placar.visitante())
                .estrategia(getNome())
                .temRecomendacao(true)
                .mensagem(String.format(
                        "Recomendação baseada no placar mais frequente do campeonato: %dx%d",
                        placar.mandante(), placar.visitante()))
                .build());
    }

    private Optional<Placar> encontrarPlacarMaisFrequente(List<Partida> finalizadas) {
        return finalizadas.stream()
                .collect(Collectors.groupingBy(
                        p -> new Placar(p.getGolsMandante(), p.getGolsVisitante()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
