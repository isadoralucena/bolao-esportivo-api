package com.ufcg.psoft.project.service.ranking;

import com.ufcg.psoft.project.comparator.ComparadorCriterioDesempateBuilder;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RankingCalculator {

    public Comparator<PontuacaoParticipanteResponseDTO> criarComparador(List<TipoCriterioDesempate> criteriosDesempate) {
        return Comparator.comparingInt(PontuacaoParticipanteResponseDTO::getPontuacao)
            .reversed()
            .thenComparing(
                ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(criteriosDesempate)
                    .build()
            );
    }

    public List<PontuacaoParticipanteResponseDTO> ordenar(List<PontuacaoParticipanteResponseDTO> participantes, List<TipoCriterioDesempate> criteriosDesempate) {

        Comparator<PontuacaoParticipanteResponseDTO> comparator = criarComparador(criteriosDesempate);
        List<PontuacaoParticipanteResponseDTO> participantesOrdenados = new ArrayList<>(participantes);
        participantesOrdenados.sort(comparator);

        return participantesOrdenados;
    }

    public Map<Long, Integer> calcularPosicoes(List<PontuacaoParticipanteResponseDTO> participantes, List<TipoCriterioDesempate> criteriosDesempate) {
        Comparator<PontuacaoParticipanteResponseDTO> comparator = criarComparador(criteriosDesempate);
        List<PontuacaoParticipanteResponseDTO> participantesOrdenados = ordenar(participantes, criteriosDesempate);

        Map<Long, Integer> posicoes = new HashMap<>();

        int posicao = 0;
        PontuacaoParticipanteResponseDTO participanteAnterior = null;

        for (int i = 0; i < participantesOrdenados.size(); i++) {
            PontuacaoParticipanteResponseDTO participanteAtual = participantesOrdenados.get(i);

            if (participanteAnterior == null || comparator.compare(participanteAnterior, participanteAtual) != 0) {
                posicao = i + 1;
            }

            posicoes.put(participanteAtual.getUsuarioId(), posicao);

            participanteAnterior = participanteAtual;
        }

        return posicoes;
    }
}