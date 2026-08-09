package com.ufcg.psoft.project.dto.partida;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Grupo;
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

    @JsonProperty("recomendacao")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RecomendacaoResponseDTO recomendacao;

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

    public PartidaResponseDTO(Partida partida, Grupo grupo, LocalDateTime agora) {
        this(partida);
        this.status = partida.statusEfetivoParaGrupo(grupo, agora);
    }
}
