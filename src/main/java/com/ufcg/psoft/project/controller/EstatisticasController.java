package com.ufcg.psoft.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ufcg.psoft.project.dto.estatisticas.EstatisticasResponseDTO;
import com.ufcg.psoft.project.service.estatisticas.EstatisticasService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
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
@Tag(name = "Estatísticas", description = "Estatísticas e evolução de desempenho dos usuários")
public class EstatisticasController {

    private final EstatisticasService estatisticasService;

    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Consultar estatísticas atuais do usuário")
    @GetMapping("/{usuarioId}/estatisticas")
    public ResponseEntity<EstatisticasResponseDTO> obterEstatisticas(@PathVariable Long usuarioId, @RequestParam String codigoUsuario) {
        var resultado = estatisticasService.obterEstatisticaMaisRecente(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Consultar evolução das estatísticas do usuário")
    @GetMapping("/{usuarioId}/estatisticas/evolucao")
    public ResponseEntity<List<EstatisticasResponseDTO>> obterEvolucao(@PathVariable Long usuarioId, @RequestParam String codigoUsuario) {
        var resultado = estatisticasService.obterEvolucaoEstatisticas(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}