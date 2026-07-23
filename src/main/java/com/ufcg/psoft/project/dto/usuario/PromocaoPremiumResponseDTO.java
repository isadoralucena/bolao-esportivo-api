package com.ufcg.psoft.project.dto.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.PromocaoPremium;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocaoPremiumResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("usuarioId")
    private Long usuarioId;

    @JsonProperty("data")
    private LocalDateTime data;

    @JsonProperty("motivo")
    private String motivo;

    @JsonProperty("palpites")
    private Integer palpites;

    @JsonProperty("gruposParticipa")
    private Integer gruposParticipa;

    @JsonProperty("requisicoes")
    private Integer requisicoes;

    @JsonProperty("acertos")
    private Integer acertos;

    public PromocaoPremiumResponseDTO(PromocaoPremium promocao) {
        this.id = promocao.getId();
        this.usuarioId = promocao.getUsuario().getId();
        this.data = promocao.getData();
        this.motivo = promocao.getMotivo();
        this.palpites = promocao.getPalpites();
        this.gruposParticipa = promocao.getGruposParticipa();
        this.requisicoes = promocao.getRequisicoes();
        this.acertos = promocao.getAcertos();
    }
}
