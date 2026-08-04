package com.ufcg.psoft.project.dto.grupo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriosDesempatePutRequestDTO {

    @JsonProperty("criteriosDesempate")
    @NotNull(message = "Critérios de desempate obrigatórios")
    @Size(min = 1, message = "Deve haver pelo menos um critério de desempate.")
    private List<TipoCriterioDesempate> criteriosDesempate;
}