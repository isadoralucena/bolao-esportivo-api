package com.ufcg.psoft.project.dto.ranking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoRankingResponseDTO {

    @JsonProperty("grupoId")
    private Long grupoId;

    @JsonProperty("snapshots")
    private List<RankingSnapshotResponseDTO> snapshots;
}