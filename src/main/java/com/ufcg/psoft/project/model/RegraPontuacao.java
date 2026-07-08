package com.ufcg.psoft.project.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_grupo_tipo_regra_pontuacao",
        columnNames = {"grupo_id", "tipoRegraPontuacao"}
    )
})
public class RegraPontuacao {
    @JsonProperty("id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
	private Long id;

    @JsonProperty("tipoRegraPontuacao")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRegraPontuacao tipoRegraPontuacao;

    @JsonProperty("pontos")
    @Column(nullable = false)
    private Integer pontos;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    @ToString.Exclude
    private Grupo grupo;
}
