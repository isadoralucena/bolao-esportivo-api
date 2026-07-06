package com.ufcg.psoft.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
public class PontuacaoPalpite {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    @JoinColumn(name = "palpite_id", nullable = false, unique = true)
    private Palpite palpite;

    @Column(nullable = false)
    @Builder.Default
    private Integer pontuacao = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean acertouVencedor = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean acertouEmpate = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean acertouPlacarExato = false;
}