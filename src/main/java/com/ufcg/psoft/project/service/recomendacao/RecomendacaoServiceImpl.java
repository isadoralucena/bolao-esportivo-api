package com.ufcg.psoft.project.service.recomendacao;

import java.util.List;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoPertenceAoCampeonatoException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoPremiumException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;
import com.ufcg.psoft.project.service.partida.PartidaService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecomendacaoServiceImpl implements RecomendacaoService {

    private final GrupoAutorizacaoService grupoAutorizacaoService;
    private final PartidaService partidaService;
    private final PartidaRepository partidaRepository;
    private final GrupoRepository grupoRepository;
    private final RecomendacaoStrategy placarFrequente;
    private final RecomendacaoStrategy mediaGols;

    public RecomendacaoServiceImpl(
            GrupoAutorizacaoService grupoAutorizacaoService,
            PartidaService partidaService,
            PartidaRepository partidaRepository,
            GrupoRepository grupoRepository,
            @Qualifier("PLACAR_FREQUENTE") RecomendacaoStrategy placarFrequente,
            @Qualifier("MEDIA_GOLS") RecomendacaoStrategy mediaGols) {
        this.grupoAutorizacaoService = grupoAutorizacaoService;
        this.partidaService = partidaService;
        this.partidaRepository = partidaRepository;
        this.grupoRepository = grupoRepository;
        this.placarFrequente = placarFrequente;
        this.mediaGols = mediaGols;
    }

    @Override
    public RecomendacaoResponseDTO recomendar(Long grupoId, Long partidaId, Long usuarioId, String codigo) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigo);
        validarPremium(usuario);
        Partida partida = obterPartida(partidaId);
        validarPartidaPertenceAoGrupo(partida, grupoId);

        RecomendacaoResponseDTO response = placarFrequente.recomendar(partida)
                .or(() -> mediaGols.recomendar(partida))
                .orElse(RecomendacaoResponseDTO.builder()
                        .golsMandanteRecomendado(null)
                        .golsVisitanteRecomendado(null)
                        .estrategia(null)
                        .temRecomendacao(false)
                        .mensagem("Não há dados suficientes para gerar uma recomendação para esta partida.")
                        .build());

        response.setPartidaId(partida.getId());
        response.setMandante(partida.getMandante());
        response.setVisitante(partida.getVisitante());
        return response;
    }

    @Override
    public RecomendacaoResponseDTO recomendar(Long partidaId, Long usuarioId, String codigo) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigo);
        validarPremium(usuario);
        Partida partida = obterPartida(partidaId);

        RecomendacaoResponseDTO response = placarFrequente.recomendar(partida)
                .or(() -> mediaGols.recomendar(partida))
                .orElse(RecomendacaoResponseDTO.builder()
                        .golsMandanteRecomendado(null)
                        .golsVisitanteRecomendado(null)
                        .estrategia(null)
                        .temRecomendacao(false)
                        .mensagem("Não há dados suficientes para gerar uma recomendação para esta partida.")
                        .build());

        response.setPartidaId(partida.getId());
        response.setMandante(partida.getMandante());
        response.setVisitante(partida.getVisitante());
        return response;
    }

    @Override
    public List<PartidaResponseDTO> listarPartidasFuturasComRecomendacao(Long usuarioId, String codigo) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigo);
        boolean isPremium = usuario.getPerfil() == PerfilUsuario.PREMIUM;

        List<PartidaResponseDTO> futuras = partidaService.listarPartidasFuturas();

        if (!isPremium) {
            return futuras;
        }

        return futuras.stream()
                .map(dto -> {
                    try {
                        RecomendacaoResponseDTO recomendacao = recomendar(dto.getId(), usuarioId, codigo);
                        dto.setRecomendacao(recomendacao);
                    } catch (RuntimeException exception) {
                        log.warn("Não foi possível gerar recomendação para uma partida futura.", exception);
                    }
                    return dto;
                })
                .toList();
    }

    private void validarPremium(Usuario usuario) {
        if (usuario.getPerfil() != PerfilUsuario.PREMIUM) {
            throw new UsuarioNaoPremiumException();
        }
    }

    private Partida obterPartida(Long partidaId) {
        return partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);
    }

    private void validarPartidaPertenceAoGrupo(Partida partida, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);
        if (!partida.getCampeonato().getId().equals(grupo.getCampeonato().getId())) {
            throw new PartidaNaoPertenceAoCampeonatoException();
        }
    }
}
