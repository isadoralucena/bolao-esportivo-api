package com.ufcg.psoft.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.ranking.RankingResponseDTO;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.ranking.RankingService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(
    value = "/ranking",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class RankingController {
    private final RankingService rankingService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping
    public ResponseEntity<RankingResponseDTO> rankingGlobal(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        var resultado = rankingService.rankingGlobal(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<RankingResponseDTO> rankingDoGrupo(
            @PathVariable Long grupoId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        var resultado = rankingService.rankingDoGrupo(grupoId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}