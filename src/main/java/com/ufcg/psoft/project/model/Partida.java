package com.ufcg.psoft.project.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Partida {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campeonato_id", nullable = false)
    private Campeonato campeonato;

    @JsonProperty("codigoExterno")
    @Column(nullable = false)
    private Long codigoExterno;

    @JsonProperty("mandante")
    @Column(nullable = false)
    private String mandante;

    @JsonProperty("visitante")
    @Column(nullable = false)
    private String visitante;

    @JsonProperty("golsMandante")
    private Integer golsMandante;

    @JsonProperty("golsVisitante")
    private Integer golsVisitante;

    @JsonProperty("data")
    @Column(nullable = false)
    private LocalDateTime data;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartidaStatus status;

    @JsonProperty("rodada")
    private Integer rodada;
}
