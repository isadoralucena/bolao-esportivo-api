package com.ufcg.psoft.project.dto.palpite;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalpitePostPutRequestDTO {

    @JsonProperty("golsMandante")
    @NotNull(message = "Gols do mandante obrigatorio")
    private Integer golsMandante;

    @JsonProperty("golsVisitante")
    @NotNull(message = "Gols do visitante obrigatorio")
    private Integer golsVisitante;
}
