package com.ufcg.psoft.project.service.grupo;

import java.util.*;

import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoInativoException;
import com.ufcg.psoft.project.exception.campeonato.CampeonatoNaoExisteException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.grupo.LimiteDeParticipantesInvalidoException;
import com.ufcg.psoft.project.exception.grupo.RegraDeTempoInvalidaException;
import com.ufcg.psoft.project.model.Campeonato;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.CampeonatoRepository;
import com.ufcg.psoft.project.repository.GrupoRepository;

@Service
@RequiredArgsConstructor
public class GrupoServiceImpl implements GrupoService {
    final GrupoRepository grupoRepository;
    private final CampeonatoRepository campeonatoRepository;
    final ModelMapper modelMapper;
    final GrupoAutorizacaoService grupoAutorizacaoService;

    public GrupoResponseDTO criar(Long usuarioId, String codigoAcesso, GrupoPostRequestDTO grupoPostRequestDto) {
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);

        Campeonato campeonato = campeonatoRepository.findById(grupoPostRequestDto.getCampeonatoId())
                .orElseThrow(CampeonatoNaoExisteException::new);

        if (!campeonato.getAtivo()) {
            throw new CampeonatoInativoException();
        }

        Grupo grupo = modelMapper.map(grupoPostRequestDto, Grupo.class);
        grupo.setCampeonato(campeonato);
        grupo.setOrganizador(usuarioLogado);
        grupo.getParticipantes().add(usuarioLogado);

        grupoRepository.save(grupo);
        return new GrupoResponseDTO(grupo);
    }

    public GrupoResponseDTO recuperar(Long usuarioId, String codigoAcesso, Long id) {
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);
        grupoAutorizacaoService.garantirAcessoLeitura(grupo, usuarioLogado);

        return new GrupoResponseDTO(grupo);
    }

    public List<GrupoResponseDTO> listar(Long usuarioId, String codigoAcesso) {
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        List<Grupo> grupos = grupoRepository.findAll();
        
        return grupos.stream()
                .filter((g -> grupoAutorizacaoService.temAcessoLeitura(g, usuarioLogado)))
                .map(GrupoResponseDTO::new)
                .toList();
    }

    public GrupoResponseDTO alterar(Long usuarioId, String codigoAcesso, Long id, GrupoPutRequestDTO grupoPutRequestDto) {
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);

        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuarioLogado);
        
        Integer novoLimite = grupoPutRequestDto.getLimiteParticipantes();
        if (novoLimite != null && novoLimite < grupo.getParticipantes().size()) {
            throw new LimiteDeParticipantesInvalidoException();
        }

        modelMapper.map(grupoPutRequestDto, grupo);
        grupo = grupoRepository.save(grupo);
        return new GrupoResponseDTO(grupo);
    }

    public void remover(Long usuarioId, String codigoAcesso, Long id) {
        Grupo grupo = grupoRepository.findById(id).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuarioLogado);

        grupoRepository.delete(grupo);
    }

    public GrupoResponseDTO configurarRegrasPalpites(Long grupoID, Long usuarioId, String codigoAcesso, RegrasPalpitesRequestDTO regrasPalpitesRequestDTO) {
        Grupo grupo = grupoRepository.findById(grupoID)
                .orElseThrow(GrupoNaoExisteException::new);

        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuario);

        if (regrasPalpitesRequestDTO.getMinutosAbertura() <= regrasPalpitesRequestDTO.getMinutosFechamento()) throw new RegraDeTempoInvalidaException();

        grupo.setMinutosAberturaPalpites(regrasPalpitesRequestDTO.getMinutosAbertura());
        grupo.setMinutosFechamentoPalpites(regrasPalpitesRequestDTO.getMinutosFechamento());
        grupoRepository.save(grupo);

        return new GrupoResponseDTO(grupo);
    }
}

