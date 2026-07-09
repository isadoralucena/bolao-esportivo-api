package com.ufcg.psoft.project.comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;

public final class ComparadorCriterioDesempateBuilder {

    private static final Map<TipoCriterioDesempate, Comparator<PontuacaoParticipanteResponseDTO>> COMPARADORES = Map.of(
        TipoCriterioDesempate.PLACAR_EXATO, Comparator.comparingInt(PontuacaoParticipanteResponseDTO::getPlacaresExatos).reversed(),
        TipoCriterioDesempate.ERRO, Comparator.comparingInt(PontuacaoParticipanteResponseDTO::getErros),
        TipoCriterioDesempate.ACERTO_VENCEDOR, Comparator.comparingInt(PontuacaoParticipanteResponseDTO::getAcertosVencedor).reversed(),
        TipoCriterioDesempate.ACERTO_EMPATE, Comparator.comparingInt(PontuacaoParticipanteResponseDTO::getAcertosEmpate).reversed()
    );

    private final List<TipoCriterioDesempate> criterios = new ArrayList<>();

    private ComparadorCriterioDesempateBuilder() {}

    public static ComparadorCriterioDesempateBuilder builder() {
        return new ComparadorCriterioDesempateBuilder();
    }

    /**
     * A ordem da lista define a prioridade: o primeiro elemento
     * é o critério de maior prioridade.
     */
    public ComparadorCriterioDesempateBuilder comCriterios(List<TipoCriterioDesempate> criteriosOrdenados) {
        this.criterios.addAll(criteriosOrdenados);
        return this;
    }

    public Comparator<PontuacaoParticipanteResponseDTO> build() {
        return criterios.stream()
            .map(this::paraComparator)
            .reduce(Comparator::thenComparing)
            .orElse((a, b) -> 0);
    }

    private Comparator<PontuacaoParticipanteResponseDTO> paraComparator(TipoCriterioDesempate tipo) {
        Comparator<PontuacaoParticipanteResponseDTO> comparator = COMPARADORES.get(tipo);
        if (comparator == null) {
            throw new IllegalArgumentException("Critério de desempate não suportado: " + tipo);
        }
        return comparator;
    }
}