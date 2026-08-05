package com.ufcg.psoft.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ufcg.psoft.project.dto.recomendacao.RecomendacaoResponseDTO;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.recomendacao.RecomendacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Recomendações", description = "Recomendações de palpites para partidas")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Obter recomendação de palpite")
    @GetMapping("/grupos/{grupoId}/partidas/{partidaId}/recomendacao")
    public ResponseEntity<RecomendacaoResponseDTO> recomendarPalpite(
            @PathVariable Long grupoId,
            @PathVariable Long partidaId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        var resultado = recomendacaoService.recomendar(grupoId, partidaId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}