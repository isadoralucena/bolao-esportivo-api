package com.ufcg.psoft.project.controller.grupo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoResponseDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;
import com.ufcg.psoft.project.dto.pontuacao.PontuacaoParticipanteResponseDTO;
import com.ufcg.psoft.project.service.grupo.GrupoService;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
@Tag(name = "Grupos", description = "Cadastro, configuração e consulta de grupos")
public class GrupoController {
    final GrupoService grupoService;
    private final PontuacaoService pontuacaoService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "Criar grupo")
    @PostMapping("")
    public ResponseEntity<GrupoResponseDTO> criarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @RequestBody @Valid GrupoPostRequestDTO grupoPostRequestDto) {
        var resultado = grupoService.criar(usuarioId, codigoUsuario, grupoPostRequestDto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado);
    }

    @Operation(summary = "Buscar grupo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> recuperarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        var resultado = grupoService.recuperar(usuarioId, codigoUsuario, id);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Listar grupos acessíveis ao usuário")
    @GetMapping("")
    public ResponseEntity<List<GrupoResponseDTO>> listarGrupos(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario) {
        var resultado = grupoService.listar(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Atualizar grupo")
    @PutMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> atualizarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long id,
            @RequestBody @Valid GrupoPutRequestDTO grupoPutRequestDto) {
        var resultado = grupoService.alterar(usuarioId, codigoUsuario, id, grupoPutRequestDto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Excluir grupo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        grupoService.remover(usuarioId, codigoUsuario, id);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

	@Operation(summary = "Configurar regras de palpites do grupo")
	@PutMapping("/{grupoId}/regras-palpites")
    public ResponseEntity<GrupoResponseDTO> configurarRegrasPalpites(
            @PathVariable Long grupoId,
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @RequestBody @Valid RegrasPalpitesRequestDTO regrasPalpitesRequestDTO) {
        var resultado = grupoService.configurarRegrasPalpites(grupoId, usuarioId, codigoUsuario, regrasPalpitesRequestDTO);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @Operation(summary = "Listar pontuações dos participantes")
    @GetMapping("/{grupoId}/pontuacoes")
    public ResponseEntity<List<PontuacaoParticipanteResponseDTO>> listarPontuacoesDoGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long grupoId) {
        var resultado = pontuacaoService.listarPontuacoesParticipantesDoGrupo(grupoId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}
