package com.ufcg.psoft.project.service.pontuacao;

import java.util.List;

import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;

public interface PontuacaoService {
    List<PalpiteResponseDTO> calcularPontuacoesAssociadasAPartida(Long partidaId);
}
