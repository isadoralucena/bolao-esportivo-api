package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.dto.ranking.HistoricoRankingResponseDTO;
import com.ufcg.psoft.project.dto.ranking.RankingSnapshotResponseDTO;
import com.ufcg.psoft.project.service.ranking.RankingHistoricoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
    public ResponseEntity<HistoricoRankingResponseDTO> obterHistorico(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterHistorico(grupoId));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<HistoricoRankingResponseDTO> obterHistoricoPorParticipante(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterHistoricoPorParticipante(grupoId, usuarioId));
    }

    @GetMapping("/lideres")
    public ResponseEntity<List<RankingSnapshotResponseDTO>> obterLideresHistoricos(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterLideresHistoricos(grupoId));
    }

    @GetMapping("/recente")
    public ResponseEntity<List<RankingSnapshotResponseDTO>> obterDesempenhoRecente(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterDesempenhoRecente(grupoId));
    }
}