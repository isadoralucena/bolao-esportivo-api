package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.partida.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

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
            @RequestParam String codigo) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(partidaService.listarPartidasFuturas(usuarioId, codigo));
    }
}
