package com.ufcg.psoft.project.service.estatisticas;

import com.ufcg.psoft.project.controller.CampeonatoController;
import com.ufcg.psoft.project.repository.GrupoRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.event.PartidaConsolidadaEvent;
import com.ufcg.psoft.project.exception.estatistica.EstatisticaNaoExisteExpcetion;
import com.ufcg.psoft.project.model.Estatisticas;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.Palpite;
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
    CampeonatoController campeonatoController;

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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoConsolidarPartida(PartidaConsolidadaEvent event) {
        this.calcularEstatisticasAssociadasAPartida(event.getPartida());
    }

    @Override
    public List<EstatisticasResponseDTO> calcularEstatisticasAssociadasAPartida(Partida p) {
        // obter o conjunto de usuarios afetados pela partida.
        // são considerados afetados usuários que estão em grupos nos quais alguem fez um palpite àquela partida.
        // pois basta um usuario do grupo ter feito o palpite e acertado para afetar o ranking do grupo, que é levado em consideração no calculo da estatística.
        List<Palpite> palpitesDaPartida = palpiteRepository.findByPartidaId(p.getId());
        List<Grupo> gruposAfetados = palpitesDaPartida.stream()
                .map(Palpite::getGrupo)
                .distinct()
                .toList();
        
        Set<Usuario> usuariosAfetados = new HashSet<>();
        for (Grupo g : gruposAfetados) {
            usuariosAfetados.addAll(g.getParticipantes());
        }


        List<Estatisticas> estatisticas = new ArrayList<>();
        for (Usuario u : usuariosAfetados) {
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

        List<Estatisticas> estatisticas = estatisticasRepository.findByUsuarioIdOrderByDataRegistroDesc(usuario.getId());

        if (estatisticas.isEmpty()) {
            throw new EstatisticaNaoExisteExpcetion();
        }

        return new EstatisticasResponseDTO(estatisticas.get(0));
    }

    @Override
    public List<EstatisticasResponseDTO> obterEvolucaoEstatisticas(Long usuarioId, String codigoAcesso) {
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);

        return estatisticasRepository.findByUsuarioIdOrderByDataRegistroAsc(usuario.getId()).stream()
            .map(EstatisticasResponseDTO::new)
            .collect(Collectors.toList());
    }

    private Estatisticas calcularEstatisticasUsuario(Usuario u) {
        PontuacaoParticipanteResponseDTO pontuacaoParticipante = pontuacaoService.calcularPontuacaoGlobalDoParticipante(u.getId());

        int totalPalpites = pontuacaoParticipante.getTotalPalpitesAvaliados();
        int totalErros = pontuacaoParticipante.getErros();

        int palpitesCorretos = totalPalpites - totalErros;
        float taxaAcerto = totalPalpites == 0 ? 0 : (float) palpitesCorretos / totalPalpites;

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

            // checa se venceu. necessário stream porque pode haver empate na primeira posição.
            boolean venceu = r.getRankingEntrys()
                .stream()
                .anyMatch(entry -> entry.getPosicao() == 1 && entry.getPontuacaoParticipante().getUsuarioId().equals(u.getId()));

            if (venceu) {
                vitorias++;
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
