package com.ufcg.psoft.project.dto.pontuacao;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PontuacaoParticipanteResponseDTO {

    @JsonProperty("grupoId")
    private Long grupoId;

    @JsonProperty("usuarioId")
    private Long usuarioId;

    @JsonProperty("pontos")
    private Integer pontos;
}