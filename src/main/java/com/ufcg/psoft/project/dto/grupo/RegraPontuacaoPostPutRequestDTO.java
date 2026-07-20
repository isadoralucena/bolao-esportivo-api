package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegraPontuacaoPostPutRequestDTO {
    @JsonProperty("tipoRegraPontuacao")
    @NotNull(message = "Tipo de regra de pontuação obrigatório")
    private TipoRegraPontuacao tipoRegraPontuacao;
    
    @JsonProperty("pontos")
    @NotNull(message = "Pontos da regra de pontuação obrigatórios")
    @Positive(message = "Os pontos da regra de pontuação devem ser um número positivo")
    private Integer pontos;
}
