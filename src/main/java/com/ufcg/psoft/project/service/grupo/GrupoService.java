package com.ufcg.psoft.project.service.grupo;

import java.util.List;
import java.util.Set;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.grupo.CriterioDesempateResponseDTO;
import com.ufcg.psoft.project.dto.grupo.ParticipantePostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoResponseDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Usuario;

public interface GrupoService {
    GrupoResponseDTO criar(Long usuarioId, String codigoAcesso, GrupoPostRequestDTO grupoPostRequestDto);
    GrupoResponseDTO recuperar(Long usuarioId, String codigoAcesso, Long id);
    List<GrupoResponseDTO> listar(Long usuarioId, String codigoAcesso);
    GrupoResponseDTO alterar(Long usuarioId, String codigoAcesso, Long id, GrupoPutRequestDTO grupoPutRequestDto);
    void remover(Long usuarioId, String codigoAcesso, Long id);
    GrupoResponseDTO adicionarParticipante(Long usuarioId, String codigoAcesso, Long grupoId, ParticipantePostRequestDTO participantePostRequestDto);
    void removerParticipante(Long usuarioId, String codigoAcesso, Long grupoId, Long participanteId);
    Set<UsuarioResponseDTO> listarParticipantes(Long usuarioId, String codigoAcesso, Long grupoId);
    GrupoResponseDTO entrarEmGrupoPublico(Long grupoId, Long usuarioId, String codigoAcesso);
    RegraPontuacaoResponseDTO inserirRegraPontuacao(Long usuarioId, String codigoAcesso, Long grupoId, RegraPontuacaoPostPutRequestDTO regraPontuacaoPostPutRequestDto);
    Set<RegraPontuacaoResponseDTO> listarRegrasPontuacao(Long usuarioId, String codigoAcesso, Long grupoId);
    void removerRegraPontuacao(Long usuarioId, String codigoAcesso, Long grupoId, Long regraPontuacaoId);
    GrupoResponseDTO configurarCriteriosDesempate(Long grupoId, Long usuarioId, String codigoAcesso, CriteriosDesempatePutRequestDTO criteriosDesempatePutRequestDTO);
    List<CriterioDesempateResponseDTO> listarCriteriosDesempate(Long usuarioId, String codigoAcesso, Long grupoId);
    GrupoResponseDTO configurarRegrasPalpites(Long grupoId, Long usuarioId, String codigo, RegrasPalpitesRequestDTO dto);
    void validarEntradaGrupo(Grupo grupo, Usuario usuario);
}