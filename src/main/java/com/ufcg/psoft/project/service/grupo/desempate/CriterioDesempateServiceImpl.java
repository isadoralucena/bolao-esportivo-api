package com.ufcg.psoft.project.service.grupo.desempate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ufcg.psoft.project.dto.grupo.CriterioDesempateResponseDTO;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.exception.grupo.CriteriosDesempateInvalidosException;
import com.ufcg.psoft.project.exception.grupo.GrupoNaoExisteException;
import com.ufcg.psoft.project.model.CriterioDesempate;
import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.TipoCriterioDesempate;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.GrupoRepository;
import com.ufcg.psoft.project.service.grupo.GrupoAutorizacaoService;

@Service
@RequiredArgsConstructor
public class CriterioDesempateServiceImpl implements CriterioDesempateService {
    final GrupoRepository grupoRepository;
    final GrupoAutorizacaoService grupoAutorizacaoService;

    public GrupoResponseDTO configurarCriteriosDesempate(Long grupoId, Long usuarioId, String codigoAcesso, CriteriosDesempatePutRequestDTO criteriosDesempatePutRequestDTO) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirOrganizador(grupo, usuario);

        List<TipoCriterioDesempate> criterios = criteriosDesempatePutRequestDTO.getCriteriosDesempate();
        validarCriteriosDesempate(criterios);
        Map<TipoCriterioDesempate, CriterioDesempate> existentesPorTipo = grupo.getCriteriosDesempate().stream()
            .collect(Collectors.toMap(CriterioDesempate::getCriterio, c -> c));

        List<CriterioDesempate> novosCriterios = new ArrayList<>();
        for (int i = 0; i < criterios.size(); i++) {
            TipoCriterioDesempate tipo = criterios.get(i);
            int novaPrioridade = i + 1;

            CriterioDesempate existente = existentesPorTipo.remove(tipo);
            if (existente != null) {
                existente.setPrioridade(novaPrioridade);
                novosCriterios.add(existente);
            } else {
                novosCriterios.add(
                    CriterioDesempate.builder()
                        .grupo(grupo)
                        .criterio(tipo)
                        .prioridade(novaPrioridade)
                        .build()
                );
            }
        }

        grupo.getCriteriosDesempate().clear();
        grupo.getCriteriosDesempate().addAll(novosCriterios);

        grupoRepository.save(grupo);
        return new GrupoResponseDTO(grupo);
    }

    public List<CriterioDesempateResponseDTO> listarCriteriosDesempate(Long usuarioId, String codigoAcesso, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(GrupoNaoExisteException::new);
        Usuario usuario = grupoAutorizacaoService.obterUsuarioValido(usuarioId, codigoAcesso);
        grupoAutorizacaoService.garantirAcessoLeitura(grupo, usuario);

        return grupo.getCriteriosDesempate().stream()
            .map(CriterioDesempateResponseDTO::new)
            .toList();
    }

    private void validarCriteriosDesempate(List<TipoCriterioDesempate> criteriosDesempate) {
        boolean invalido = criteriosDesempate == null
            || criteriosDesempate.isEmpty()
            || criteriosDesempate.stream().anyMatch(Objects::isNull)
            || EnumSet.copyOf(criteriosDesempate).size() != criteriosDesempate.size();

        if (invalido) {
            throw new CriteriosDesempateInvalidosException();
        }
    }
}
