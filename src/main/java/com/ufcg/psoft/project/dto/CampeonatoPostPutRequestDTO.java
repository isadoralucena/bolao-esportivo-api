package com.ufcg.psoft.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampeonatoPostPutRequestDTO {

    @JsonProperty("nome")
    @NotBlank(message = "Nome obrigatorio")
    private String nome;

    @JsonProperty("url")
    @NotBlank(message = "url obrigatoria")
    private String url;

    @JsonProperty("codigo")
    @NotNull(message = "Codigo de time obrigatorio")
    private String codigo;

    @JsonProperty("ativo")
    private Boolean ativo;
}
