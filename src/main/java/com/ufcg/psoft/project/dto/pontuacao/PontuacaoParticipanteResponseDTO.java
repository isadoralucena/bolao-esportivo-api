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
    @JsonProperty("usuarioId")
    private Long usuarioId;

    @JsonProperty("usuarioNome")
    private String usuarioNome;

    @JsonProperty("totalPalpitesAvaliados")
    private int totalPalpitesAvaliados;

    @JsonProperty("pontuacao")
    private int pontuacao;

    @JsonProperty("erros")
    private int erros;

    @JsonProperty("acertosVencedor")
    private int acertosVencedor;

    @JsonProperty("acertosEmpate")
    private int acertosEmpate;

    @JsonProperty("placaresExatos")
    private int placaresExatos;

    public PontuacaoParticipanteResponseDTO(
            Usuario usuario,
            int totalPalpitesAvaliados,
            int pontuacao,
            int erros,
            int acertosVencedor,
            int acertosEmpate,
            int placaresExatos
    ) {
        this.usuarioId = usuario.getId();
        this.totalPalpitesAvaliados = totalPalpitesAvaliados;
        this.usuarioNome = usuario.getNome();
        this.pontuacao = pontuacao;
        this.erros = erros;
        this.acertosVencedor = acertosVencedor;
        this.acertosEmpate = acertosEmpate;
        this.placaresExatos = placaresExatos;
    }
}