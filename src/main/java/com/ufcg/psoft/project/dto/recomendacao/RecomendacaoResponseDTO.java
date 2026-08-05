package com.ufcg.psoft.project.dto.recomendacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacaoResponseDTO {

    @JsonProperty("partidaId")
    private Long partidaId;

    @JsonProperty("mandante")
    private String mandante;

    @JsonProperty("visitante")
    private String visitante;

    @JsonProperty("golsMandanteRecomendado")
    private Integer golsMandanteRecomendado;

    @JsonProperty("golsVisitanteRecomendado")
    private Integer golsVisitanteRecomendado;

    @JsonProperty("estrategia")
    private String estrategia;

    @JsonProperty("temRecomendacao")
    private boolean temRecomendacao;

    @JsonProperty("mensagem")
    private String mensagem;
}