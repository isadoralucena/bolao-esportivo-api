package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriosDesempateResponseDTO {

	@JsonProperty("tipoCriterioDesempate")
	private TipoCriterioDesempate tipoCriterioDesempate;

	@JsonProperty("prioridade")
	private Integer prioridade;
}
