package com.ufcg.psoft.project.dto.campeonato;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Campeonato;
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

    @JsonProperty("ultimaSincronizacao")
    private LocalDateTime ultimaSincronizacao;

	public CampeonatoResponseDTO(Campeonato campeonato) {
		this.id = campeonato.getId();
		this.nome = campeonato.getNome();
		this.url = campeonato.getUrl();
		this.codigo = campeonato.getCodigo();
		this.ativo = campeonato.getAtivo();
        this.ultimaSincronizacao = campeonato.getUltimaSincronizacao();
	}
}
