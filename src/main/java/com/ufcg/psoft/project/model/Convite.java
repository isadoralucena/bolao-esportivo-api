package com.ufcg.psoft.project.model;

import org.hibernate.annotations.ManyToAny;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Convite {

    @JsonProperty("id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

    @JsonProperty("descricao")
	@Column(nullable = false)
	private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConvite status = StatusConvite.PENDENTE;

    @OneToMany(mappedBy = "grupo")
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @OneToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;

    @ManyToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    @JoinColumn(name = "convidade_id", nullable = false)
    private Usuario convidado;
}
