package com.ufcg.psoft.project.service.grupo;

import java.util.List;
import java.util.Set;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.grupo.ParticipantePostRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;


public interface GrupoService {
    GrupoResponseDTO criar(GrupoPostRequestDTO grupoPostRequestDto, String codigoAcesso);

    GrupoResponseDTO recuperar(Long id, String codigoAcesso);

    List<GrupoResponseDTO> listar(String codigoAcesso);

    GrupoResponseDTO alterar(Long id, GrupoPutRequestDTO grupoPutRequestDto, String codigoAcesso);
    
    void remover(Long id, String codigoAcesso);

    GrupoResponseDTO adicionarParticipante(Long grupoId, ParticipantePostRequestDTO participantePostRequestDto, String codigoAcesso);

    void removerParticipante(Long grupoId, Long usuarioId, String codigoAcesso);

    Set<UsuarioResponseDTO> listarParticipantes(Long grupoId, String codigoAcesso);
}
