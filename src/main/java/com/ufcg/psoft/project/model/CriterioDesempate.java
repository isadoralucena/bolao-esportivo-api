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
        name = "uk_criterio_desempate_grupo",
        columnNames = {"grupo_id", "criterio"}
    )
})
public class CriterioDesempate {
    @JsonProperty("id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
	private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
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