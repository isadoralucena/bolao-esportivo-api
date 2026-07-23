package com.ufcg.psoft.project.service.estatisticas;

import com.ufcg.psoft.project.repository.GrupoRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.event.PartidaConsolidadaEvent;
import com.ufcg.psoft.project.model.Estatisticas;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PontuacaoPalpite;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.EstatisticasRepository;
import com.ufcg.psoft.project.repository.PalpiteRepository;
import com.ufcg.psoft.project.repository.PontuacaoPalpiteRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import com.ufcg.psoft.project.service.ranking.RankingService;

import java.util.stream.Collectors;

@Service
public class EstatisticasServiceImpl implements EstatisticasService {

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private PalpiteRepository palpiteRepository;

    @Autowired
    private EstatisticasRepository estatisticasRepository;

    @Autowired
    private GrupoAutorizacaoService grupoAutorizacaoService;

    @Autowired
    private PontuacaoService pontuacaoService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private PontuacaoPalpiteRepository pontuacaoPalpiteRepository;

    EstatisticasServiceImpl(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public List<EstatisticasResponseDTO> calcularEstatisticasAssociadasAConsolidacaoDePartida(PartidaConsolidadaEvent event) {
        Partida p = event.getPartida();

        // obtem o conjunto de usuarios que criaram palpites para a partida em questão
        List<Usuario> usuarios = palpiteRepository.findDistinctUsuarioByPartidaId(p.getId());

        List<Estatisticas> estatisticas = new ArrayList<>();
        for (Usuario u : usuarios) {
            Estatisticas e = calcularEstatisticasUsuario(u);
            estatisticas.add(e);
            estatisticasRepository.save(e);
        }
        
        return estatisticas.stream()
            .map(EstatisticasResponseDTO::new)
            .collect(Collectors.toList());
    }

    @Override
    public EstatisticasResponseDTO obterEstatisticaMaisRecente(Long usuarioId, String codigoAcesso) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        Estatisticas estatisticaAtual = estatisticasRepository.findFirstByUsuarioIdOrderByDataRegistroDesc(usuario.getId());
        return new EstatisticasResponseDTO(estatisticaAtual);
    }

    @Override
    public List<EstatisticasResponseDTO> obterEvolucaoEstatisticas(Long usuarioId, String codigoAcesso) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);

        return estatisticasRepository.findByUsuarioId(usuario.getId()).stream()
            .map(EstatisticasResponseDTO::new)
            .collect(Collectors.toList());
    }

    private Estatisticas calcularEstatisticasUsuario(Usuario u) {
        PontuacaoParticipanteResponseDTO pontuacaoParticipante = pontuacaoService.calcularPontuacaoGlobalDoParticipante(u.getId());

        int totalPalpites = pontuacaoParticipante.getTotalPalpitesAvaliados();
        int totalErros = pontuacaoParticipante.getErros();

        float taxaAcerto = 1 - (totalErros / totalPalpites);
        int palpitesCorretos = totalPalpites - totalErros;

        Estatisticas e = Estatisticas.builder()
            .usuario(u)
            .taxaAcerto(taxaAcerto)
            .placaresExatos(pontuacaoParticipante.getPlacaresExatos())
            .vitoriasRankings(contarVitoriasRankings(u))
            .maiorSequenciaAcertos(contarSequenciaAcertos(u))
            .totalPalpitesCorretos(palpitesCorretos)
            .dataRegistro(LocalDateTime.now(ZoneOffset.UTC))
            .build();
        
        return e;
    }


    private int contarVitoriasRankings(Usuario u) {
        List<Grupo> grupos = grupoRepository.findByParticipantes_Id(u.getId());
        
        int vitorias = 0;
        for (Grupo g : grupos) {
            RankingResponseDTO r = rankingService.rankingDoGrupo(g.getId(), u.getId(), u.getCodigo());
            if (r.getRankingEntrys().get(0).getPontuacaoParticipante().getUsuarioId() == u.getId()) {
                vitorias += 1;
            }
        }

        return vitorias;
    }

    private int contarSequenciaAcertos(Usuario u) {
        List<PontuacaoPalpite> pontuacoes = pontuacaoPalpiteRepository.findByPalpite_Usuario_IdOrderByPalpite_DataAsc(u.getId());

        int sequenciaAtual = 0;
        int maiorSequencia = 0;
        for (PontuacaoPalpite p : pontuacoes) {
            boolean acertouAlgo = p.isAcertouEmpate() || p.isAcertouPlacarExato() || p.isAcertouVencedor();
            if (acertouAlgo) {
                sequenciaAtual += 1;
                if (sequenciaAtual > maiorSequencia) {
                    maiorSequencia = sequenciaAtual;
                }
            } else {
                sequenciaAtual = 0;
            }
        }

        return maiorSequencia;
    }
}
