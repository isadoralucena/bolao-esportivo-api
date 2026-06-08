package com.ufcg.psoft.project.dto.campeonato;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Campeonato;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampeonatoResponseDTO {
	private Long id;

	@JsonProperty("nome")
	private String nome;

	@JsonProperty("url")
	private String url;

	@JsonProperty("codigo")
	private String codigo;

	@JsonProperty("ativo")
	private Boolean ativo;

	public CampeonatoResponseDTO(Campeonato campeonato) {
		this.id = campeonato.getId();
		this.nome = campeonato.getNome();
		this.url = campeonato.getUrl();
		this.codigo = campeonato.getCodigo();
		this.ativo = campeonato.getAtivo();
	}
}
