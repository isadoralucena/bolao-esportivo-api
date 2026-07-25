package com.ufcg.psoft.project.model;

import java.time.LocalDateTime;

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
public class Estatisticas {
    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @JsonProperty("taxaAcerto")
    private float taxaAcerto;

    @JsonProperty("placaresExatos")
    private int placaresExatos;

    @JsonProperty("vitoriasRankings")
    private int vitoriasRankings;

    @JsonProperty("maiorSequenciaAcertos")
    private int maiorSequenciaAcertos;

    @JsonProperty("totalPalpitesCorretos")
    private int totalPalpitesCorretos;

    @JsonProperty("dataRegistro")
    private LocalDateTime dataRegistro;
}
