package com.ufcg.psoft.project.model;

import java.util.HashSet;
import java.util.Set;

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
public class Grupo {
    @JsonProperty("id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

    @JsonProperty("nome")
	@Column(nullable = false)
	private String nome;

    @JsonProperty("descricao")
	@Column(nullable = false)
	private String descricao;

	@ManyToOne
	@JoinColumn(name = "campeonato_id", nullable = false)
	private Campeonato campeonato;

	@JsonProperty("privacidade")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PrivacidadeGrupo privacidade;

	@JsonProperty("limiteParticipantes")
	@Column(nullable = true)
	private Integer limiteParticipantes;

	@ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
	private Usuario organizador;

	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "grupo_participantes",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
	@Builder.Default
    private Set<Usuario> participantes = new HashSet<>();

	@JsonProperty("minutosAberturaPalpite")
	@Column(nullable = false)
	@Builder.Default
	private Integer minutosAberturaPalpites = 120;

	@JsonProperty("minutosFechamentoPalpite")
	@Column(nullable = false)
	@Builder.Default
	private Integer minutosFechamentoPalpites = 0;

	public JanelaDePalpites getJanelaDePalpites() {
    	return new JanelaDePalpites(this.minutosAberturaPalpites, this.minutosFechamentoPalpites);
	}
}