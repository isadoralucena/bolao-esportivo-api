package com.ufcg.psoft.project.service.palpite;

import com.ufcg.psoft.project.dto.palpite.PalpitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;
import com.ufcg.psoft.project.exception.*;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PalpiteServiceImpl implements PalpiteService {

    @Autowired
    private PalpiteRepository palpiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private PartidaRepository partidaRepository;

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
        
        if (!partida.estaAbertaParaPalpite(grupo.getJanelaDePalpites(), LocalDateTime.now(ZoneOffset.UTC))) {
            throw new PalpiteForaDoTempoException();
        }

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
                .data(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        palpiteRepository.save(palpite);
        return new PalpiteResponseDTO(palpite);
    }

    @Override
    public List<PalpiteResponseDTO> listarPorGrupo(Long grupoId) {
        return palpiteRepository.findByGrupoId(grupoId).stream()
                .map(PalpiteResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PalpiteResponseDTO> listarPorGrupoEPartida(Long grupoId, Long partidaId) {
        return palpiteRepository.findByPartidaIdAndGrupoId(partidaId, grupoId).stream()
                .map(PalpiteResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PalpiteResponseDTO> listarPorUsuario(Long usuarioId) {
        return palpiteRepository.findByUsuarioId(usuarioId).stream()
                .map(PalpiteResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public PalpiteResponseDTO editar(Long palpiteId, Long usuarioId, String codigo, PalpitePostPutRequestDTO dto) {
        Palpite palpite = obterPalpiteValidado(palpiteId, usuarioId, codigo);

        palpite.setGolsMandante(dto.getGolsMandante());
        palpite.setGolsVisitante(dto.getGolsVisitante());

        palpiteRepository.save(palpite);
        return new PalpiteResponseDTO(palpite);
    }

    @Override
    public void deletar(Long palpiteId, Long usuarioId, String codigo) {
        Palpite palpite = obterPalpiteValidado(palpiteId, usuarioId, codigo);
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

        if (!palpite.getPartida().estaAbertaParaPalpite(palpite.getGrupo().getJanelaDePalpites(), LocalDateTime.now(ZoneOffset.UTC))) {
            throw new PalpiteForaDoTempoException();
        }

        return palpite;
    }
}
