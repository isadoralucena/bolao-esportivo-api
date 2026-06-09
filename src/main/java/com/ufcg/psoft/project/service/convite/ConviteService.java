package com.ufcg.psoft.project.service.convite;

import java.util.List;

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;

public interface ConviteService {
   
    ConviteResponseDTO criar(ConvitePostPutRequestDTO convitePostPutRequestDTO);

    void remover(Long id, Long idOrganizador);

    ConviteResponseDTO aceitar(Long id, Long idConvidado);

    ConviteResponseDTO recusar(Long id, Long idConvidado);

    ConviteResponseDTO ignorar(Long id, Long idConvidado);

    List<ConviteResponseDTO> listarConvitesPendentesPorConvidado(Long convidadoId);

}
