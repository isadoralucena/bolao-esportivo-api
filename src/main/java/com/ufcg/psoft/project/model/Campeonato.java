package com.ufcg.psoft.project.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	@JsonIgnore
	@OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Partida> partidas = new ArrayList<>();
}
