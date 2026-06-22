package com.ufcg.psoft.project.dto.palpite;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegrasPalpitesRequestDTO {
    @NotNull @Min(0)
    private Integer minutosAbertura;

    @NotNull @Min(0)
    private Integer minutosFechamento;
}