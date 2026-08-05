package com.ufcg.psoft.project.service.palpite;

import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;
import com.ufcg.psoft.project.exception.*;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.palpite.PalpiteJaExisteException;
import com.ufcg.psoft.project.exception.palpite.PalpiteNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoPertenceAoCampeonatoException;
import com.ufcg.psoft.project.exception.usuario.UsuarioInvalidoException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoParticipanteException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PalpiteServiceImpl implements PalpiteService {

    private final PalpiteRepository palpiteRepository;

    private final UsuarioRepository usuarioRepository;

    private final GrupoRepository grupoRepository;

    private final PartidaRepository partidaRepository;

    private final Clock clock;

    @Override
    public PalpiteResponseDTO criar(Long usuarioId, String codigo, Long grupoId, Long partidaId, PalpitePostPutRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);

        if (!usuario.getCodigo().equals(codigo)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);

        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);
        
        partida.validarCriacaoPalpite(grupo.getJanelaDePalpites(), LocalDateTime.now(clock));

        if (!grupo.getParticipantes().contains(usuario)) {
            throw new UsuarioNaoParticipanteException();
        }

        if (!partida.getCampeonato().getId().equals(grupo.getCampeonato().getId())) {
            throw new PartidaNaoPertenceAoCampeonatoException();
        }

        if (palpiteRepository.existsByUsuarioIdAndPartidaIdAndGrupoId(usuarioId, partidaId, grupoId)) {
            throw new PalpiteJaExisteException();
        }

        Palpite palpite = Palpite.builder()
                .partida(partida)
                .usuario(usuario)
                .grupo(grupo)
                .golsMandante(dto.getGolsMandante())
                .golsVisitante(dto.getGolsVisitante())
                .data(LocalDateTime.now(clock))
                .build();

        palpiteRepository.save(palpite);
        return new PalpiteResponseDTO(palpite);
    }

    @Override
    public List<PalpiteResponseDTO> listarPorGrupo(Long grupoId) {
        return palpiteRepository.findByGrupoId(grupoId).stream()
                .map(PalpiteResponseDTO::new)
                .toList();
    }

    @Override
    public List<PalpiteResponseDTO> listarPorGrupoEPartida(Long grupoId, Long partidaId) {
        return palpiteRepository.findByPartidaIdAndGrupoId(partidaId, grupoId).stream()
                .map(PalpiteResponseDTO::new)
                .toList();
    }

    @Override
    public List<PalpiteResponseDTO> listarPorUsuario(Long usuarioId) {
        return palpiteRepository.findByUsuarioId(usuarioId).stream()
                .map(PalpiteResponseDTO::new)
                .toList();
    }

    @Override
    public PalpiteResponseDTO editar(Long palpiteId, Long usuarioId, String codigo, PalpitePostPutRequestDTO dto) {
        Palpite palpite = obterPalpiteValidado(palpiteId, usuarioId, codigo);

        palpite.getPartida().validarEdicaoPalpite(
                palpite.getGrupo().getJanelaDePalpites(),
                LocalDateTime.now(clock));

        palpite.setGolsMandante(dto.getGolsMandante());
        palpite.setGolsVisitante(dto.getGolsVisitante());

        palpiteRepository.save(palpite);
        return new PalpiteResponseDTO(palpite);
    }

    @Override
    public void deletar(Long palpiteId, Long usuarioId, String codigo) {
        Palpite palpite = obterPalpiteValidado(palpiteId, usuarioId, codigo);

        palpite.getPartida().validarExclusaoPalpite(
                palpite.getGrupo().getJanelaDePalpites(),
                LocalDateTime.now(clock));

        palpiteRepository.delete(palpite);
    }

    private Palpite obterPalpiteValidado(Long palpiteId, Long usuarioId, String codigo) {
        Palpite palpite = palpiteRepository.findById(palpiteId)
                .orElseThrow(PalpiteNaoExisteException::new);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);

        if (!usuario.getCodigo().equals(codigo)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        if (!palpite.getUsuario().getId().equals(usuarioId)) {
            throw new UsuarioInvalidoException();
        }

        return palpite;
    }
}
