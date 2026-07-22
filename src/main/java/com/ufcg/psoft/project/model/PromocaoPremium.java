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
    @UniqueConstraint(columnNames = {"usuario_id"})
})
public class PromocaoPremium {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @JsonProperty("data")
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime data = LocalDateTime.now();

    @JsonProperty("motivo")
    @Column(nullable = false)
    private String motivo;

    @JsonProperty("palpites")
    @Column(nullable = false)
    private Integer palpites;

    @JsonProperty("gruposParticipa")
    @Column(nullable = false)
    private Integer gruposParticipa;

    @JsonProperty("requisicoes")
    @Column(nullable = false)
    private Integer requisicoes;

    @JsonProperty("acertos")
    @Column(nullable = false)
    private Integer acertos;
}
