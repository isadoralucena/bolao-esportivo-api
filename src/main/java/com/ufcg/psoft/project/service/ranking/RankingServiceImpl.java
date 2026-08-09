package com.ufcg.psoft.project.service.ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingEntryResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.usuario.UsuarioNaoExisteException;
import com.ufcg.psoft.project.model.CriterioDesempate;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final PontuacaoService pontuacaoService;

    private final GrupoRepository grupoRepository;

    private final UsuarioRepository usuarioRepository;

    private final RankingCalculator rankingCalculator;

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
        List<PontuacaoParticipanteResponseDTO> pontuacoesOrdenadas = rankingCalculator.ordenar(pontuacoesParticipantes, criteriosDesempate);
        Map<Long, Integer> posicoes = rankingCalculator.calcularPosicoes(pontuacoesParticipantes, criteriosDesempate);
        List<RankingEntryResponseDTO> rankingEntries = new ArrayList<>();

        for (PontuacaoParticipanteResponseDTO participante : pontuacoesOrdenadas) {
            Integer posicao = posicoes.get(participante.getUsuarioId());
            rankingEntries.add(new RankingEntryResponseDTO(posicao, participante));
        }

        return new RankingResponseDTO(grupoId, rankingEntries);
    }
    private Usuario obterUsuario(Long usuarioId, String codigoAcesso) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(UsuarioNaoExisteException::new);
        if (!usuario.getCodigo().equals(codigoAcesso)) throw new CodigoDeAcessoInvalidoException();
        return usuario;
    }
    
}
