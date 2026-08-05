package com.ufcg.psoft.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ufcg.psoft.project.dto.partida.PartidaResponseDTO;
import com.ufcg.psoft.project.service.partida.PartidaService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.recomendacao.RecomendacaoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Partidas", description = "Consulta de partidas dos campeonatos e grupos")
public class PartidaController {

    private final PartidaService partidaService;

    private final RecomendacaoService recomendacaoService;

    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Listar partidas do campeonato")
    @GetMapping("/campeonatos/{campeonatoId}/partidas")
    public ResponseEntity<List<PartidaResponseDTO>> listarPartidasDoCampeonato(@PathVariable Long campeonatoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(partidaService.listarPorCampeonato(campeonatoId));
    }

    @Operation(summary = "Listar partidas do grupo")
    @GetMapping("/grupos/{grupoId}/partidas")
    public ResponseEntity<List<PartidaResponseDTO>> listarPartidasDoGrupo(@PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(partidaService.listarPorGrupo(grupoId));
    }

    @Operation(summary = "Listar próximas partidas do usuário")
    @GetMapping("/partidas/futuras")
    public ResponseEntity<List<PartidaResponseDTO>> listarPartidasFuturas(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        var resultado = recomendacaoService.listarPartidasFuturasComRecomendacao(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}