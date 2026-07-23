package com.ufcg.psoft.project.service.grupo.pontuacao;

import java.util.Set;

import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoResponseDTO;

public interface RegraPontuacaoService {
    RegraPontuacaoResponseDTO inserirRegraPontuacao(Long usuarioId, String codigoAcesso, Long grupoId, RegraPontuacaoPostPutRequestDTO regraPontuacaoPostPutRequestDto);
    Set<RegraPontuacaoResponseDTO> listarRegrasPontuacao(Long usuarioId, String codigoAcesso, Long grupoId);
    void removerRegraPontuacao(Long usuarioId, String codigoAcesso, Long grupoId, Long regraPontuacaoId);
}
