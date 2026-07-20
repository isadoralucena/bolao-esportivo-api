package com.ufcg.psoft.project.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campeonato {
	@JsonProperty("id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@JsonProperty("nome")
	@Column(nullable = false)
	private String nome;

	@JsonProperty("url")
	@Column(nullable = false)
	private String url;
	
	@JsonProperty("codigo")
	@Column(nullable = false)
	private String codigo;

	@JsonProperty("ativo")
	@Builder.Default
	private Boolean ativo = false;
}
