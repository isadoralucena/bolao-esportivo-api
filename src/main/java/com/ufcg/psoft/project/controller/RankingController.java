package com.ufcg.psoft.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.ranking.RankingService;

import org.springframework.beans.factory.annotation.Autowired;
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
public class RankingController {
    @Autowired
    private RankingService rankingService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping
    public ResponseEntity<?> rankingGlobal(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso) {
        var resultado = rankingService.rankingGlobal(usuarioId, codigoAcesso);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<?> rankingDoGrupo(
            @PathVariable Long grupoId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso) {
        var resultado = rankingService.rankingDoGrupo(grupoId, usuarioId, codigoAcesso);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}
