package com.ufcg.psoft.project.dto.convite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.StatusConvite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConviteResponseDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("grupo")
    private Long grupo;

    @JsonProperty("organizador")
    private Long organizador;

    @JsonProperty("convidado")
    private Long convidado;

    @JsonProperty("status")
    private StatusConvite status;
}