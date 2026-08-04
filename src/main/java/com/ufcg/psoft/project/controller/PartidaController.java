package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.partida.PartidaService;
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
public class PartidaController {

    private final PartidaService partidaService;

    private final RecomendacaoService recomendacaoService;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/campeonatos/{campeonatoId}/partidas")
    public ResponseEntity<?> listarPartidasDoCampeonato(@PathVariable Long campeonatoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(partidaService.listarPorCampeonato(campeonatoId));
    }

    @GetMapping("/grupos/{grupoId}/partidas")
    public ResponseEntity<?> listarPartidasDoGrupo(@PathVariable Long grupoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(partidaService.listarPorGrupo(grupoId));
    }

    @GetMapping("/partidas/futuras")
    public ResponseEntity<?> listarPartidasFuturas(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        var resultado = recomendacaoService.listarPartidasFuturasComRecomendacao(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}
