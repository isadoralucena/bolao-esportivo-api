package com.ufcg.psoft.project.dto.convite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Usuario;

import jakarta.validation.constraints.NotNull;
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
    
    @NotNull(message = "Grupo obrigatorio")
    private Long grupo;

    @NotNull(message = "Organizador obrigatorio")
    private Long organizador;

    @NotNull(message = "Convidado obrigatorio")
    private Long convidado;
}
