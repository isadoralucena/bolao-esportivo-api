package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;

import jakarta.validation.constraints.NotBlank;
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
public class GrupoPostRequestDTO {
    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatório")
    private String nome;

    @JsonProperty("descricao")
    @NotBlank(message = "Descricao obrigatória")
    private String descricao;

    @JsonProperty("privacidade")
    @NotNull(message = "Privacidade obrigatoria")
    private PrivacidadeGrupo privacidade;

    @JsonProperty("limiteParticipantes")
    @Positive(message = "O limite deve ser maior que zero")
    private Integer limiteParticipantes;

    @JsonProperty("campeonatoId")
    @NotNull(message = "O campeonato associado é obrigatório")
    private Long campeonatoId;
}