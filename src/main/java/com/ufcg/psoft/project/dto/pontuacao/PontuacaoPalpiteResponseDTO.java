package com.ufcg.psoft.project.dto.pontuacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.PontuacaoPalpite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PontuacaoPalpiteResponseDTO {

    @JsonProperty("palpiteId")
    private Long palpiteId;

    @JsonProperty("usuarioId")
    private Long usuarioId;

    @JsonProperty("grupoId")
    private Long grupoId;

    @JsonProperty("partidaId")
    private Long partidaId;

    @JsonProperty("pontuacao")
    private Integer pontuacao;

    @JsonProperty("acertouVencedor")
    private Boolean acertouVencedor;

    @JsonProperty("acertouEmpate")
    private Boolean acertouEmpate;

    @JsonProperty("acertouPlacarExato")
    private Boolean acertouPlacarExato;

    public PontuacaoPalpiteResponseDTO(PontuacaoPalpite pontuacaoPalpite) {
        this.palpiteId = pontuacaoPalpite.getPalpite().getId();
        this.usuarioId = pontuacaoPalpite.getPalpite().getUsuario().getId();
        this.grupoId = pontuacaoPalpite.getPalpite().getGrupo().getId();
        this.partidaId = pontuacaoPalpite.getPalpite().getPartida().getId();
        this.pontuacao = pontuacaoPalpite.getPontuacao();
        this.acertouVencedor = pontuacaoPalpite.getAcertouVencedor();
        this.acertouEmpate = pontuacaoPalpite.getAcertouEmpate();
        this.acertouPlacarExato = pontuacaoPalpite.getAcertouPlacarExato();
    }
}