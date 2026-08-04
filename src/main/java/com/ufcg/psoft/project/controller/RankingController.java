package com.ufcg.psoft.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.service.ranking.RankingService;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping
    public ResponseEntity<?> rankingGlobal(
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingService.rankingGlobal(usuarioId, codigoAcesso));
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<?> rankingDoGrupo(
            @PathVariable Long grupoId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoAcesso) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingService.rankingDoGrupo(grupoId, usuarioId, codigoAcesso));
    }
}
