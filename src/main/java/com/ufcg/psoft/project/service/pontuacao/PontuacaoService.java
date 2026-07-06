package com.ufcg.psoft.project.service.pontuacao;

import java.util.List;

import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;

public interface PontuacaoService {
    List<PalpiteResponseDTO> calcularPontuacoesAssociadasAPartida(Long partidaId);
    List<PontuacaoParticipanteResponseDTO> listarPontuacoesDoGrupo(Long grupoId, Long usuarioId, String codigoAcesso);
    List<PalpiteResponseDTO> calcularPontuacoesDoGrupo(Long grupoId);
}
