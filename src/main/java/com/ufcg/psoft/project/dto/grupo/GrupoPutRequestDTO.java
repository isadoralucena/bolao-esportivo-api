package com.ufcg.psoft.project.dto.grupo;

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
public class GrupoPutRequestDTO {
    @NotBlank(message = "Nome obrigatório")
    private String nome;

    @NotBlank(message = "Descrição obrigatória")
    private String descricao;

    @NotNull(message = "Privacidade obrigatória")
    private PrivacidadeGrupo privacidade;

    @Positive(message = "O limite deve ser maior que zero")
    private Integer limiteParticipantes;
}