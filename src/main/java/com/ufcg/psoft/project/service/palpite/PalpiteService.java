package com.ufcg.psoft.project.service.palpite;

import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;

import java.util.List;

public interface PalpiteService {

    PalpiteResponseDTO criar(Long usuarioId, String codigo, Long grupoId, Long partidaId, PalpitePostPutRequestDTO dto);

    List<PalpiteResponseDTO> listarPorGrupo(Long grupoId);

    List<PalpiteResponseDTO> listarPorGrupoEPartida(Long grupoId, Long partidaId);

    List<PalpiteResponseDTO> listarPorUsuario(Long usuarioId);

    PalpiteResponseDTO editar(Long palpiteId, Long usuarioId, String codigo, PalpitePostPutRequestDTO dto);
}
