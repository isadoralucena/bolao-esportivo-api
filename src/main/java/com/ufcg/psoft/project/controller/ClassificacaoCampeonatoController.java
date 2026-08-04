package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    value = "/campeonatos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class ClassificacaoCampeonatoController {

    private final ClassificacaoCampeonatoService classificacaoCampeonatoService;

    @GetMapping("/{campeonatoId}/classificacao")
    public ResponseEntity<?> listarClassificacao(@PathVariable Long campeonatoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(classificacaoCampeonatoService.listarPorCampeonato(campeonatoId));
    }
}
