package com.ufcg.psoft.project.controller;

import com.ufcg.psoft.project.service.estatisticas.EstatisticasService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EstatisticasController {

    private final EstatisticasService estatisticasService;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/{usuarioId}/estatisticas")
    public ResponseEntity<?> obterEstatisticas(@PathVariable Long usuarioId, @RequestParam String codigoUsuario) {
        var resultado = estatisticasService.obterEstatisticaMaisRecente(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @GetMapping("/{usuarioId}/estatisticas/evolucao")
    public ResponseEntity<?> obterEvolucao(@PathVariable Long usuarioId, @RequestParam String codigoUsuario) {
        var resultado = estatisticasService.obterEvolucaoEstatisticas(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}
