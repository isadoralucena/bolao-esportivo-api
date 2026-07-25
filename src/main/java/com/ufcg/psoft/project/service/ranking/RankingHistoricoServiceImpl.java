package com.ufcg.psoft.project.service.ranking;

import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.ranking.HistoricoRankingResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingSnapshotResponseDTO;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.partida.PartidaNaoExisteException;
import com.ufcg.psoft.project.model.*;
import com.ufcg.psoft.project.repository.*;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RankingHistoricoServiceImpl implements RankingHistoricoService {

    @Autowired
    private RankingSnapshotRepository rankingSnapshotRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PontuacaoService pontuacaoService;

    @Autowired
    private RankingCalculator rankingCalculator;

    @Value("${project.ranking.historico.desempenho-recente-partidas:5}")
    private int desempenhoRecentePartidas;

    @Override
    public HistoricoRankingResponseDTO obterHistorico(Long grupoId) {
        validarGrupo(grupoId);
        List<RankingSnapshot> snapshots = rankingSnapshotRepository
                .findByGrupoIdOrderByDataSnapshotAscPosicaoAsc(grupoId);
        return toHistoricoDTO(grupoId, snapshots);
    }

    @Override
    public HistoricoRankingResponseDTO obterHistoricoPorParticipante(Long grupoId, Long usuarioId) {
        validarGrupo(grupoId);
        List<RankingSnapshot> snapshots = rankingSnapshotRepository
                .findByGrupoIdAndUsuarioIdOrderByDataSnapshotAsc(grupoId, usuarioId);
        return toHistoricoDTO(grupoId, snapshots);
    }

    @Override
    public List<RankingSnapshotResponseDTO> obterLideresHistoricos(Long grupoId) {
        validarGrupo(grupoId);
        return rankingSnapshotRepository
                .findByGrupoIdAndPosicaoOrderByDataSnapshotAsc(grupoId, 1)
                .stream()
                .map(RankingSnapshotResponseDTO::new)
                .toList();
    }

    @Override
    public List<RankingSnapshotResponseDTO> obterDesempenhoRecente(Long grupoId) {
        validarGrupo(grupoId);
        List<RankingSnapshot> todos = rankingSnapshotRepository
                .findByGrupoIdOrderByDataSnapshotDescPosicaoAsc(grupoId);

        List<Long> partidasRecentes = todos.stream()
                .map(s -> s.getPartida().getId())
                .distinct()
                .limit(desempenhoRecentePartidas)
                .toList();

        return todos.stream()
                .filter(s -> partidasRecentes.contains(s.getPartida().getId()))
                .map(RankingSnapshotResponseDTO::new)
                .toList();
    }

    @Override
    @Transactional
    public void gerarSnapshot(Long grupoId, Long partidaId) {
        if (rankingSnapshotRepository.existsByGrupoIdAndPartidaId(grupoId, partidaId)) {
            return;
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);

        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(PartidaNaoExisteException::new);

        List<PontuacaoParticipanteResponseDTO> pontuacoes = pontuacaoService
                .listarPontuacoesParticipantesDoGrupo(grupoId, grupo.getOrganizador().getId(), grupo.getOrganizador().getCodigo());

        List<TipoCriterioDesempate> criterios = grupo.getCriteriosDesempate().stream()
                .map(CriterioDesempate::getCriterio)
                .toList();

        Map<Long, Integer> posicoes = rankingCalculator.calcularPosicoes(pontuacoes, criterios);

        List<RankingSnapshot> snapshots = new ArrayList<>();
        for (PontuacaoParticipanteResponseDTO pontuacao : pontuacoes) {
            snapshots.add(RankingSnapshot.builder()
                    .grupo(grupo)
                    .usuario(grupoRepository.findById(grupoId)
                            .map(g -> g.getParticipantes().stream()
                                    .filter(u -> u.getId().equals(pontuacao.getUsuarioId()))
                                    .findFirst()
                                    .orElseThrow())
                            .orElseThrow())
                    .partida(partida)
                    .posicao(posicoes.get(pontuacao.getUsuarioId()))
                    .pontuacao(pontuacao.getPontuacao())
                    .dataSnapshot(LocalDateTime.now())
                    .build());
        }

        rankingSnapshotRepository.saveAll(snapshots);
    }

    private void validarGrupo(Long grupoId) {
        if (!grupoRepository.existsById(grupoId)) {
            throw new GrupoNaoExisteException();
        }
    }

    private HistoricoRankingResponseDTO toHistoricoDTO(Long grupoId, List<RankingSnapshot> snapshots) {
        return HistoricoRankingResponseDTO.builder()
                .grupoId(grupoId)
                .snapshots(snapshots.stream()
                        .map(RankingSnapshotResponseDTO::new)
                        .toList())
                .build();
    }
}