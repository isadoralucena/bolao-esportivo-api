package com.ufcg.psoft.project.dto.grupo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantePostRequestDTO {
    @NotNull(message = "O id do participante é obrigatório")
    private Long usuarioId;
}
