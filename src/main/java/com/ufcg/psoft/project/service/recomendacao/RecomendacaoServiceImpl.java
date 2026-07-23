package com.ufcg.psoft.project.service.recomendacao;

import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.exception.RecomendacaoEstrategiaInvalidaException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.model.PerfilUsuario;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.ufcg.psoft.project.exception.partida.PartidaNaoPertenceAoCampeonatoException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoPremiumException;
import com.ufcg.psoft.project.model.Grupo;

@Service
public class RecomendacaoServiceImpl implements RecomendacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private Map<String, RecomendacaoStrategy> estrategias;

    @Override
    public RecomendacaoResponseDTO recomendar(Long grupoId, Long partidaId, Long usuarioId, String codigo, String estrategia) {
        Usuario usuario = obterUsuarioValido(usuarioId, codigo);
        validarPremium(usuario);
        Partida partida = obterPartida(partidaId);
        validarPartidaPertenceAoGrupo(partida, grupoId);
        RecomendacaoStrategy strategy = obterEstrategia(estrategia);
        return strategy.recomendar(partida);
    }

    private Usuario obterUsuarioValido(Long usuarioId, String codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);
        if (!usuario.getCodigo().equals(codigo)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        return usuario;
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

    private RecomendacaoStrategy obterEstrategia(String estrategia) {
        RecomendacaoStrategy strategy = estrategias.get(estrategia);
        if (strategy == null) {
            throw new RecomendacaoEstrategiaInvalidaException();
        }
        return strategy;
    }

    private void validarPartidaPertenceAoGrupo(Partida partida, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);
        if (!partida.getCampeonato().getId().equals(grupo.getCampeonato().getId())) {
            throw new PartidaNaoPertenceAoCampeonatoException();
        }
    }
}