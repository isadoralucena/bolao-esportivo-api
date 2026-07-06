package com.ufcg.psoft.project.dto.palpite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Palpite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalpiteResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("partidaId")
    private Long partidaId;

    @JsonProperty("usuarioId")
    private Long usuarioId;

    @JsonProperty("usuarioNome")
    private String usuarioNome;

    @JsonProperty("grupoId")
    private Long grupoId;

    @JsonProperty("golsMandante")
    private Integer golsMandante;

    @JsonProperty("golsVisitante")
    private Integer golsVisitante;

    @JsonProperty("data")
    private LocalDateTime data;

    public PalpiteResponseDTO(Palpite palpite) {
        this.id = palpite.getId();
        this.partidaId = palpite.getPartida().getId();
        this.usuarioId = palpite.getUsuario().getId();
        this.usuarioNome = palpite.getUsuario().getNome();
        this.grupoId = palpite.getGrupo().getId();
        this.golsMandante = palpite.getGolsMandante();
        this.golsVisitante = palpite.getGolsVisitante();
        this.data = palpite.getData();
    }
}
