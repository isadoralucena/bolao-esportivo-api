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
@Table(uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_grupo_tipo_regra",
        columnNames = {"grupo_id", "criterio"}
    )
})
class GrupoCriterioDesempate {
    @JsonProperty("id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Grupo grupo;

    @JsonProperty("tipoCriterioDesempate")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCriterioDesempate criterio;

    @JsonProperty("prioridade")
    @Column(nullable = false)
    private Integer prioridade;
}