package com.ufcg.psoft.project.dto.partida;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartidaResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("mandante")
    private String mandante;

    @JsonProperty("visitante")
    private String visitante;

    @JsonProperty("golsMandante")
    private Integer golsMandante;

    @JsonProperty("golsVisitante")
    private Integer golsVisitante;

    @JsonProperty("data")
    private LocalDateTime data;

    @JsonProperty("status")
    private PartidaStatus status;

    @JsonProperty("mataMata")
    private boolean mataMata;

    public PartidaResponseDTO(Partida partida) {
        this.id = partida.getId();
        this.mandante = partida.getMandante();
        this.visitante = partida.getVisitante();
        this.golsMandante = partida.getGolsMandante();
        this.golsVisitante = partida.getGolsVisitante();
        this.data = partida.getData();
        this.status = partida.getStatus();
        this.mataMata = partida.isMataMata();
    }
}
