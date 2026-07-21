package com.ufcg.psoft.project.service.grupo.pontuacao;

import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoResponseDTO;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.exception.grupo.RegraPontuacaoDuplicadaException;
import com.ufcg.psoft.project.exception.grupo.RegraPontuacaoNaoExisteException;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.RegraPontuacao;
import com.ufcg.psoft.project.model.TipoRegraPontuacao;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.repository.RegraPontuacaoRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;

@Service
public class RegraPontuacaoServiceImpl implements RegraPontuacaoService {
    @Autowired
    GrupoRepository grupoRepository;
    @Autowired
    GrupoAutorizacaoService grupoAutorizacaoService;
    @Autowired
    RegraPontuacaoRepository regraPontuacaoRepository;
    @Autowired
    private PontuacaoService pontuacaoService;
    @Autowired
    ModelMapper modelMapper;

    public RegraPontuacaoResponseDTO inserirRegraPontuacao(Long usuarioId, String codigoAcesso, Long grupoId, RegraPontuacaoPostPutRequestDTO regraPontuacaoPostPutRequestDto) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuarioLogado);

        TipoRegraPontuacao tipo = regraPontuacaoPostPutRequestDto.getTipoRegraPontuacao();

        if (regraPontuacaoRepository.existsByGrupoAndTipoRegraPontuacao(grupo, tipo)) {
            throw new RegraPontuacaoDuplicadaException();
        }

        RegraPontuacao regraPontuacao = modelMapper.map(regraPontuacaoPostPutRequestDto, RegraPontuacao.class);
        regraPontuacao.setGrupo(grupo);
        regraPontuacao = regraPontuacaoRepository.save(regraPontuacao);
        
        pontuacaoService.calcularPontuacoesDoGrupo(grupoId);

        return modelMapper.map(regraPontuacao, RegraPontuacaoResponseDTO.class);
    }

    public Set<RegraPontuacaoResponseDTO> listarRegrasPontuacao(Long usuarioId, String codigoAcesso, Long grupoId){
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirAcessoLeitura(grupo, usuarioLogado);

        return grupo.getRegrasPontuacao().stream()
                .map(regra -> modelMapper.map(regra, RegraPontuacaoResponseDTO.class))
                .collect(Collectors.toSet());
    }

    public void removerRegraPontuacao(Long usuarioId, String codigoAcesso, Long grupoId, Long regraPontuacaoId){
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuarioLogado = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuarioLogado);

        RegraPontuacao regraPontuacao = regraPontuacaoRepository.findById(regraPontuacaoId)
                .filter(regra -> regra.getGrupo().getId().equals(grupoId))
                .orElseThrow(RegraPontuacaoNaoExisteException::new);

        regraPontuacaoRepository.delete(regraPontuacao);
        pontuacaoService.calcularPontuacoesDoGrupo(grupoId);
    }
}
