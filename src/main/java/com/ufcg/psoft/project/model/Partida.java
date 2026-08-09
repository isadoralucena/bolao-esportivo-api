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

    @JsonProperty("consolidada")
    @Builder.Default
    private boolean consolidada = false;

    @JsonProperty("data")
    @Column(nullable = false)
    private LocalDateTime data;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartidaStatus status;

    @JsonProperty("mataMata")
    @Column(nullable = false)
    @Builder.Default
    private boolean mataMata = false;

    public boolean estaAbertaParaPalpite(JanelaDePalpites janela, LocalDateTime horaAtual) {
        return this.status != null
                && this.status.getEstado().estaAbertaParaPalpite(this, janela, horaAtual);
    }

    public void validarCriacaoPalpite(JanelaDePalpites janela, LocalDateTime horaAtual) {
        this.status.getEstado().validarCriacaoPalpite(this, janela, horaAtual);
    }

    public void validarEdicaoPalpite(JanelaDePalpites janela, LocalDateTime horaAtual) {
        this.status.getEstado().validarEdicaoPalpite(this, janela, horaAtual);
    }

    public void validarExclusaoPalpite(JanelaDePalpites janela, LocalDateTime horaAtual) {
        this.status.getEstado().validarExclusaoPalpite(this, janela, horaAtual);
    }

    public void validarConsolidacao() {
        this.status.getEstado().validarConsolidacao();
    }

    public PartidaStatus statusEfetivoParaGrupo(Grupo grupo, LocalDateTime agora) {
        return this.status == null
                ? null
                : this.status.getEstado().statusEfetivoParaGrupo(this, grupo, agora);
    }
}
