package com.ufcg.psoft.project.service.grupo.participante;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoInativoException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.grupo.LimiteDeParticipantesAtingidoException;
import com.ufcg.psoft.project.exception.grupo.PermissaoNegadaException;
import com.ufcg.psoft.project.exception.partida.PartidasInvalidasException;
import com.ufcg.psoft.project.exception.usuario.UsuarioJaParticipanteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.model.PrivacidadeGrupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;

@Service
@RequiredArgsConstructor
public class GrupoParticipanteServiceImpl implements GrupoParticipanteService {
    final GrupoRepository grupoRepository;
    final GrupoAutorizacaoService grupoAutorizacaoService;
    private final UsuarioRepository usuarioRepository;
    private final PartidaRepository partidaRepository;

    public GrupoResponseDTO entrarEmGrupoPublico(Long grupoId, Long usuarioId, String codigoAcesso) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);

        if (grupo.getPrivacidade() == PrivacidadeGrupo.PRIVADA) {
            throw new PermissaoNegadaException();
        }

        validarEntradaGrupo(grupo, usuarioLogado);

        grupo.getParticipantes().add(usuarioLogado);
        grupoRepository.save(grupo);
        return new GrupoResponseDTO(grupo);
    }

    public void removerParticipante(Long usuarioId, String codigoAcesso, Long grupoId, Long participanteId) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuarioLogado);

        Usuario participante = usuarioRepository.findById(participanteId)
        .orElseThrow(UsuarioNaoExisteException::new);

        if (grupo.getOrganizador().equals(participante)) { // impede remoçao do proprio organizador
            throw new PermissaoNegadaException();
        }

        grupo.getParticipantes().remove(participante);
        grupoRepository.save(grupo);
    }

    public Set<UsuarioResponseDTO> listarParticipantes(Long usuarioId, String codigoAcesso, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirAcessoLeitura(grupo, usuario);

        return grupo.getParticipantes().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toSet());
    }

    public void validarEntradaGrupo(Grupo grupo, Usuario usuario) {
        if (!Boolean.TRUE.equals(grupo.getCampeonato().getAtivo())) {
            throw new CampeonatoInativoException();
        }

        boolean temPartidasSincronizadas = partidaRepository.existsByCampeonatoId(grupo.getCampeonato().getId());
        if (temPartidasSincronizadas) {
            boolean temPartidasValidas = partidaRepository.existsByCampeonatoIdAndStatusIn(
                    grupo.getCampeonato().getId(),
                    List.of(PartidaStatus.ABERTO, PartidaStatus.EM_ANDAMENTO));
            if (!temPartidasValidas) {
                throw new PartidasInvalidasException();
            }
        }

        if (grupo.getParticipantes().contains(usuario)) {
            throw new UsuarioJaParticipanteException();
        }

        if (grupo.getLimiteParticipantes() != null && grupo.getParticipantes().size() >= grupo.getLimiteParticipantes()) {
            throw new LimiteDeParticipantesAtingidoException();
        }
    }
}
