package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.ranking.RankingHistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/grupos/{grupoId}/ranking/historico",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class RankingHistoricoController {

    private final RankingHistoricoService rankingHistoricoService;

    @GetMapping
    public ResponseEntity<?> obterHistorico(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterHistorico(grupoId));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<?> obterHistoricoPorParticipante(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterHistoricoPorParticipante(grupoId, usuarioId));
    }

    @GetMapping("/lideres")
    public ResponseEntity<?> obterLideresHistoricos(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterLideresHistoricos(grupoId));
    }

    @GetMapping("/recente")
    public ResponseEntity<?> obterDesempenhoRecente(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterDesempenhoRecente(grupoId));
    }
}
