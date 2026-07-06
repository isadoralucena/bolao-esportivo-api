package com.ufcg.psoft.project.dto.pontuacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Usuario;

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

    @JsonProperty("usuarioNome")
    private String usuarioNome;

    @JsonProperty("pontuacao")
    private Integer pontuacao;

    @JsonProperty("acertosVencedor")
    private Integer acertosVencedor;

    @JsonProperty("acertosEmpate")
    private Integer acertosEmpate;

    @JsonProperty("placaresExatos")
    private Integer placaresExatos;

    public PontuacaoParticipanteResponseDTO(
            Long grupoId,
            Usuario usuario,
            Integer pontuacao,
            Integer acertosVencedor,
            Integer acertosEmpate,
            Integer placaresExatos
    ) {
        this.grupoId = grupoId;
        this.usuarioId = usuario.getId();
        this.usuarioNome = usuario.getNome();
        this.pontuacao = pontuacao;
        this.acertosVencedor = acertosVencedor;
        this.acertosEmpate = acertosEmpate;
        this.placaresExatos = placaresExatos;
    }
}