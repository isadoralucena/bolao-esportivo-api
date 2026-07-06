package com.ufcg.psoft.project.service.pontuacao;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.PartidaNaoExisteException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PartidaRepository;
import com.ufcg.psoft.project.repository.RegraPontuacaoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class PontuacaoServiceImpl implements PontuacaoService {
    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PalpiteRepository palpiteRepository;

    @Autowired
    private RegraPontuacaoRepository regraPontuacaoRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private List<Pontuador> pontuadoresDisponiveis;

    private Map<TipoRegraPontuacao, Pontuador> pontuadores;

    @PostConstruct
    public void inicializarPontuadores() {
        this.pontuadores = new EnumMap<>(TipoRegraPontuacao.class);

        for (Pontuador pontuador : pontuadoresDisponiveis) {
            this.pontuadores.put(pontuador.getTipo(), pontuador);
        }
    }

    @Override
    @Transactional
    public List<PalpiteResponseDTO> calcularPontuacoesAssociadasAPartida(Long partidaId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);

        if (partida.getStatus() != PartidaStatus.FINALIZADO) {
            return List.of();
        }

        if (partida.getGolsMandante() == null || partida.getGolsVisitante() == null) {
            throw new IllegalArgumentException("Os gols de uma partida não podem ser nulos!");
        }

        List<Palpite> palpites = palpiteRepository.findByPartidaId(partidaId);

        for (Palpite palpite : palpites) {
            int pontos = calcularPontuacaoPalpite(palpite);
            palpite.setPontos(pontos);
        }

        List<Palpite> palpitesAtualizados = palpiteRepository.saveAll(palpites);

        return palpitesAtualizados.stream()
                .map(PalpiteResponseDTO::new)
                .toList();
    }

    @Override
    @Transactional
    public List<PalpiteResponseDTO>  calcularPontuacoesDoGrupo(Long grupoId) {
        List<Palpite> palpites = palpiteRepository.findByGrupoId(grupoId);

        for (Palpite palpite : palpites) {
            Partida partida = palpite.getPartida();

            if (partida.getStatus() == PartidaStatus.FINALIZADO) {
                palpite.setPontos(calcularPontuacaoPalpite(palpite));
            }
        }

        palpiteRepository.saveAll(palpites);

        return palpites.stream()
            .map(PalpiteResponseDTO::new)
            .toList();
    }

    @Override   
    public List<PontuacaoParticipanteResponseDTO> listarPontuacoesDoGrupo(Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);

        List<Palpite> palpites = palpiteRepository.findByGrupoId(grupoId);

        Map<Long, Integer> pontosPorUsuario = new HashMap<>();
        for (Palpite palpite : palpites) {
            Long usuarioId = palpite.getUsuario().getId();
            Integer pontos = palpite.getPontos();

            pontosPorUsuario.put(
                usuarioId,
                pontosPorUsuario.getOrDefault(usuarioId, 0) + pontos
            );
        }

        List<PontuacaoParticipanteResponseDTO> pontuacoes = new ArrayList<>();
        for (Usuario usuario : grupo.getParticipantes()) {
            PontuacaoParticipanteResponseDTO dto = PontuacaoParticipanteResponseDTO.builder()
                    .grupoId(grupo.getId())
                    .usuarioId(usuario.getId())
                    .pontos(pontosPorUsuario.getOrDefault(usuario.getId(), 0))
                    .build();

            pontuacoes.add(dto);
        }

        return pontuacoes;
    }

    private int calcularPontuacaoPalpite(Palpite palpite) {
        List<RegraPontuacao> regras = regraPontuacaoRepository.findByGrupoId(palpite.getGrupo().getId());

        int total = 0;

        for (RegraPontuacao regra : regras) {
            Pontuador pontuador = pontuadores.get(regra.getTipoRegraPontuacao());
            total += pontuador.calcular(palpite.getPartida(), palpite, regra.getPontos());
        }

        return total;
    }
}
