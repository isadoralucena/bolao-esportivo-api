package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.CriterioDesempate;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriterioDesempateResponseDTO {

	@JsonProperty("criterio")
	private TipoCriterioDesempate criterio;

	@JsonProperty("prioridade")
	private Integer prioridade;

	public CriterioDesempateResponseDTO(CriterioDesempate criterio) {
        this.criterio = criterio.getCriterio();
        this.prioridade = criterio.getPrioridade();
    }
}
