package com.ufcg.psoft.project.service.pontuacao;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.palpite.PalpiteResponseDTO;
import com.ufcg.psoft.project.exception.PartidaNaoExisteException;
import com.ufcg.psoft.project.model.Palpite;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;
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
