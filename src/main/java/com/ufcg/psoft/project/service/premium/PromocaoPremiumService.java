package com.ufcg.psoft.project.service.premium;

import com.ufcg.psoft.project.dto.usuario.PromocaoPremiumResponseDTO;

public interface PromocaoPremiumService {

    void avaliarPromocoes();

    PromocaoPremiumResponseDTO obterPromocao(Long usuarioId);
}
