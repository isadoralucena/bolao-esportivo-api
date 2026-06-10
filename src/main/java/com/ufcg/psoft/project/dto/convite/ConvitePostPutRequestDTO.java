package com.ufcg.psoft.project.dto.convite;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvitePostPutRequestDTO {

    @JsonProperty("descricao")
    private String descricao;
    
    @JsonProperty("grupo")
    @NotBlank(message = "Grupo obrigatorio")
    private Long grupo;

    @JsonProperty("organizador")
    @NotBlank(message = "Organizador obrigatorio")
    private Long organizador;

    @JsonProperty("convidado")
    @NotBlank(message = "Convidado obrigatorio")
    private Long convidado;
}
