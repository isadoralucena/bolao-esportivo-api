package com.ufcg.psoft.project.service.grupo;

import java.util.List;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;

public interface GrupoService {
    GrupoResponseDTO criar(Long usuarioId, String codigoAcesso, GrupoPostRequestDTO grupoPostRequestDto);
    GrupoResponseDTO recuperar(Long usuarioId, String codigoAcesso, Long id);
    List<GrupoResponseDTO> listar(Long usuarioId, String codigoAcesso);
    GrupoResponseDTO alterar(Long usuarioId, String codigoAcesso, Long id, GrupoPutRequestDTO grupoPutRequestDto);
    void remover(Long usuarioId, String codigoAcesso, Long id);
    GrupoResponseDTO configurarRegrasPalpites(Long grupoId, Long usuarioId, String codigo, RegrasPalpitesRequestDTO dto);
}