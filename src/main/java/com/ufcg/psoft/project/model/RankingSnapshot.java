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
public class RankingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @JsonProperty("posicao")
    @Column(nullable = false)
    private int posicao;

    @JsonProperty("pontuacao")
    @Column(nullable = false)
    private int pontuacao;

    @JsonProperty("dataSnapshot")
    @Column(nullable = false)
    private LocalDateTime dataSnapshot;
}