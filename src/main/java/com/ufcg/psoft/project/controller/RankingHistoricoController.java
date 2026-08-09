package com.ufcg.psoft.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Histórico de Rankings", description = "Evolução temporal dos rankings dos grupos")
public class RankingHistoricoController {
    private final RankingHistoricoService rankingHistoricoService;

    @Operation(summary = "Consultar histórico do ranking do grupo")
    @GetMapping
    public ResponseEntity<HistoricoRankingResponseDTO> obterHistorico(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterHistorico(grupoId));
    }

    @Operation(summary = "Consultar evolução de um participante")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<HistoricoRankingResponseDTO> obterHistoricoPorParticipante(
            @PathVariable Long grupoId,
            @PathVariable Long usuarioId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterHistoricoPorParticipante(grupoId, usuarioId));
    }

    @Operation(summary = "Consultar líderes históricos do grupo")
    @GetMapping("/lideres")
    public ResponseEntity<List<RankingSnapshotResponseDTO>> obterLideresHistoricos(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterLideresHistoricos(grupoId));
    }

    @Operation(summary = "Consultar desempenho recente do grupo")
    @GetMapping("/recente")
    public ResponseEntity<List<RankingSnapshotResponseDTO>> obterDesempenhoRecente(
            @PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(rankingHistoricoService.obterDesempenhoRecente(grupoId));
    }
}