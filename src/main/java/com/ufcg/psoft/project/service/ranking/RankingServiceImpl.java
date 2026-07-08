package com.ufcg.psoft.project.service.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ufcg.psoft.project.comparator.ComparadorCriterioDesempateBuilder;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingEntryResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.CriterioDesempate;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;

public class RankingServiceImpl implements RankingService {

    @Autowired
    private PontuacaoService pontuacaoService;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final List<TipoCriterioDesempate> CRITERIOS_DESEMPATE = List.of(
        TipoCriterioDesempate.PLACAR_EXATO,
        TipoCriterioDesempate.ACERTO_VENCEDOR,
        TipoCriterioDesempate.ACERTO_EMPATE,
        TipoCriterioDesempate.ERRO
    );

    @Override
    public RankingResponseDTO rankingDoGrupo(Long grupoId, Long usuarioId, String codigoAcesso) {
        List<PontuacaoParticipanteResponseDTO> pontuacoesParticipantes = pontuacaoService.listarPontuacoesParticipantesDoGrupo(grupoId, usuarioId, codigoAcesso);

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoExisteException::new);
        
        List<TipoCriterioDesempate> criteriosDesempate = grupo.getCriteriosDesempate().stream()
            .map(CriterioDesempate::getCriterio)
            .toList();

        return construirRanking(grupoId, pontuacoesParticipantes, criteriosDesempate);
    }

    @Override
    public RankingResponseDTO rankingGlobal(Long usuarioId, String codigoAcesso) {
        this.obterUsuario(usuarioId, codigoAcesso);
        List<PontuacaoParticipanteResponseDTO> pontuacoesParticipantes = pontuacaoService.listarPontuacoesGlobais();
        return construirRanking(null, pontuacoesParticipantes, CRITERIOS_DESEMPATE);
    }

    private RankingResponseDTO construirRanking(Long grupoId, List<PontuacaoParticipanteResponseDTO> pontuacoesParticipantes, List<TipoCriterioDesempate> criteriosDesempate) {
        Comparator<PontuacaoParticipanteResponseDTO> comparator = 
            Comparator.comparingInt(PontuacaoParticipanteResponseDTO::getPontuacao).reversed()
                .thenComparing(ComparadorCriterioDesempateBuilder.builder()
                    .comCriterios(criteriosDesempate)
                    .build());

        List<PontuacaoParticipanteResponseDTO> pontuacoesOrdenadas = pontuacoesParticipantes.stream()
            .sorted(comparator)
            .toList();

        List<RankingEntryResponseDTO> rankingEntrys = new ArrayList<>();

        int posicao = 0;
        PontuacaoParticipanteResponseDTO participanteAnterior = null;

        for (int i = 0; i < pontuacoesOrdenadas.size(); i++) {
            PontuacaoParticipanteResponseDTO participanteAtual = pontuacoesOrdenadas.get(i);
            
            if (participanteAnterior == null || comparator.compare(participanteAnterior, participanteAtual) != 0) {
                posicao = i + 1;
            }

            rankingEntrys.add(new RankingEntryResponseDTO(posicao, participanteAtual));
            participanteAnterior = participanteAtual;
        }

        return new RankingResponseDTO(grupoId, rankingEntrys);
    }

    private Usuario obterUsuario(Long usuarioId, String codigoAcesso) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);
        if (!usuario.getCodigo().equals(codigoAcesso)) throw new CodigoDeAcessoInvalidoException();
        return usuario;
    }
    
}
