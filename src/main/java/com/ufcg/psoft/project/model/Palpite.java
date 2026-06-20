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
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"partida_id", "usuario_id", "grupo_id"})
})
public class Palpite {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @JsonProperty("golsMandante")
    @Column(nullable = false)
    private Integer golsMandante;

    @JsonProperty("golsVisitante")
    @Column(nullable = false)
    private Integer golsVisitante;

    @JsonProperty("data")
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime data = LocalDateTime.now();
}
