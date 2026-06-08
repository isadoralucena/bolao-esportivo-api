package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Privacidade;

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
    private Privacidade privacidade;

    @Positive(message = "O limite deve ser maior que zero")
    private Integer limiteParticipantes;

    @NotNull(message = "O campeonato associado é obrigatório")
    private Long campeonatoId;

    @NotNull(message = "O organizador/criador do grupo é obrigatório")
    private Long organizadorId;
}