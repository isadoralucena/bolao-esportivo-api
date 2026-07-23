package com.ufcg.psoft.project.service.grupo.desempate;

import java.util.List;

import com.ufcg.psoft.project.dto.grupo.CriterioDesempateResponseDTO;
import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;

public interface CriterioDesempateService {
    GrupoResponseDTO configurarCriteriosDesempate(Long grupoId, Long usuarioId, String codigoAcesso, CriteriosDesempatePutRequestDTO criteriosDesempatePutRequestDTO);
    List<CriterioDesempateResponseDTO> listarCriteriosDesempate(Long usuarioId, String codigoAcesso, Long grupoId);
}
