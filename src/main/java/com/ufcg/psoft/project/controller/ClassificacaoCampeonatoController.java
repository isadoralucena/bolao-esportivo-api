package com.ufcg.psoft.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ufcg.psoft.project.dto.campeonato.ClassificacaoCampeonatoResponseDTO;
import com.ufcg.psoft.project.service.campeonato.ClassificacaoCampeonatoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
@Tag(name = "Classificação do Campeonato", description = "Consulta da classificação oficial dos campeonatos")
public class ClassificacaoCampeonatoController {

    private final ClassificacaoCampeonatoService classificacaoCampeonatoService;

    @Operation(summary = "Listar classificação do campeonato")
    @GetMapping("/{campeonatoId}/classificacao")
    public ResponseEntity<List<ClassificacaoCampeonatoResponseDTO>> listarClassificacao(@PathVariable Long campeonatoId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(classificacaoCampeonatoService.listarPorCampeonato(campeonatoId));
    }
}