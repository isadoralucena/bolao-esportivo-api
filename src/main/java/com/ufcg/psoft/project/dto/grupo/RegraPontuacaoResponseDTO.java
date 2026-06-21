package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegraPontuacaoResponseDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("tipoRegraPontuacao")
    private TipoRegraPontuacao tipoRegraPontuacao;
    @JsonProperty("pontos")
    private Integer pontos;
}
