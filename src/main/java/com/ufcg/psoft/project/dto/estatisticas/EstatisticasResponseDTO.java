package com.ufcg.psoft.project.dto.estatisticas;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Estatisticas;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Usuario;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasResponseDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("usuario_id")
    private Long usuarioId;

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

    public EstatisticasResponseDTO(Estatisticas estatisticas) {
        this.id = estatisticas.getId();
        this.usuarioId = estatisticas.getUsuario().getId();
        this.taxaAcerto = estatisticas.getTaxaAcerto();
        this.placaresExatos = estatisticas.getPlacaresExatos();
        this.vitoriasRankings = estatisticas.getVitoriasRankings();
        this.maiorSequenciaAcertos = estatisticas.getMaiorSequenciaAcertos();
        this.totalPalpitesCorretos = estatisticas.getTotalPalpitesCorretos();
        this.dataRegistro = estatisticas.getDataRegistro();
    }
}