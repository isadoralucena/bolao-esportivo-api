package com.ufcg.psoft.project.service.convite;

import java.util.List;

import com.ufcg.psoft.project.dto.convite.ConvitePostPutRequestDTO;
import com.ufcg.psoft.project.dto.convite.ConviteResponseDTO;

public interface ConviteService {
   
    ConviteResponseDTO criar(String codigoAcesso, ConvitePostPutRequestDTO convitePostPutRequestDTO);

    void remover(Long id, String codigoAcesso);

    ConviteResponseDTO aceitar(Long id, String codigoAcesso);

    ConviteResponseDTO recusar(Long id, String codigoAcesso);

    ConviteResponseDTO ignorar(Long id, String codigoAcesso);

    List<ConviteResponseDTO> listarConvitesPendentesPorConvidado(Long convidadoId, String codigoAcesso);

}
