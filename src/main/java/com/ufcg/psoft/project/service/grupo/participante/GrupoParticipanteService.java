package com.ufcg.psoft.project.service.grupo.participante;

import java.util.Set;

import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Usuario;

public interface GrupoParticipanteService {
    GrupoResponseDTO entrarEmGrupoPublico(Long grupoId, Long usuarioId, String codigoAcesso);
    void removerParticipante(Long usuarioId, String codigoAcesso, Long grupoId, Long participanteId);
    Set<UsuarioResponseDTO> listarParticipantes(Long usuarioId, String codigoAcesso, Long grupoId);
    void validarEntradaGrupo(Grupo grupo, Usuario usuario);
}