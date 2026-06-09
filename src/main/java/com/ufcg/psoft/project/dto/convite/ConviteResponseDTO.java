package com.ufcg.psoft.project.dto.convite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.StatusConvite;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @JsonProperty("grupo")
    @NotBlank(message = "Grupo obrigatorio")
    private Long grupo;

    @JsonProperty("organizador")
    @NotBlank(message = "Organizador obrigatorio")
    private Long organizador;

    @JsonProperty("convidado")
    @NotBlank(message = "Convidado obrigatorio")
    private Long convidado;

    @JsonProperty("status")
    @NotBlank(message = "Status obrigatorio")
    private StatusConvite status;
}
