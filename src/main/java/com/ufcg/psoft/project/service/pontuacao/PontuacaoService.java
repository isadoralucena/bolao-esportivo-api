package com.ufcg.psoft.project.service.pontuacao;

import java.util.List;

import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoPalpiteResponseDTO;

public interface PontuacaoService {
    List<PontuacaoPalpiteResponseDTO> calcularPontuacoesAssociadasAPartida(Long partidaId);
    List<PontuacaoPalpiteResponseDTO> calcularPontuacoesDoGrupo(Long grupoId);
    PontuacaoParticipanteResponseDTO calcularPontuacaoParticipanteNoGrupo(Long grupoId, Long participanteId);
    List<PontuacaoParticipanteResponseDTO> listarPontuacoesParticipantesDoGrupo(Long grupoId, Long usuarioId, String codigoAcesso);
}
