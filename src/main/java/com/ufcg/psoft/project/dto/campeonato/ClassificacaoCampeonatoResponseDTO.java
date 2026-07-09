package com.ufcg.psoft.project.dto.campeonato;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.ClassificacaoCampeonato;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificacaoCampeonatoResponseDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("campeonatoId")
    private Long campeonatoId;

    @JsonProperty("posicao")
    private Integer posicao;

    @JsonProperty("nomeTime")
    private String nomeTime;

    public ClassificacaoCampeonatoResponseDTO(ClassificacaoCampeonato classificacao) {
        this.id = classificacao.getId();
        this.campeonatoId = classificacao.getCampeonato().getId();
        this.posicao = classificacao.getPosicao();
        this.nomeTime = classificacao.getNomeTime();
    }
}
