package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.estatisticas.EstatisticasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class EstatisticasController {
    @Autowired
    private EstatisticasService estatisticasService;

    @GetMapping("/{usuarioId}/estatisticas")
    public ResponseEntity<?> obterEstatisticas(@PathVariable Long usuarioId, @RequestParam String codigoUsuario) {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(estatisticasService.obterEstatisticaMaisRecente(usuarioId, codigoUsuario)
        );
    }

    @GetMapping("/{usuarioId}/estatisticas/evolucao")
    public ResponseEntity<?> obterEvolucao(@PathVariable Long usuarioId, @RequestParam String codigoUsuario) {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(estatisticasService.obterEvolucaoEstatisticas(usuarioId, codigoUsuario)
        );
    }
}
