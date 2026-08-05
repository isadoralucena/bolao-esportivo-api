package com.ufcg.psoft.project.dto.ranking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.RankingSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingSnapshotResponseDTO {

    @JsonProperty("usuarioId")
    private Long usuarioId;

    @JsonProperty("usuarioNome")
    private String usuarioNome;

    @JsonProperty("partidaId")
    private Long partidaId;

    @JsonProperty("posicao")
    private int posicao;

    @JsonProperty("pontuacao")
    private int pontuacao;

    @JsonProperty("dataSnapshot")
    private LocalDateTime dataSnapshot;

    public RankingSnapshotResponseDTO(RankingSnapshot snapshot) {
        this.usuarioId = snapshot.getUsuario().getId();
        this.usuarioNome = snapshot.getUsuario().getNome();
        this.partidaId = snapshot.getPartida() != null ? snapshot.getPartida().getId() : null;
        this.posicao = snapshot.getPosicao();
        this.pontuacao = snapshot.getPontuacao();
        this.dataSnapshot = snapshot.getDataSnapshot();
    }
}