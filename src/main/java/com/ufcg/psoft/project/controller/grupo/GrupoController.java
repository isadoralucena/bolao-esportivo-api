package com.ufcg.psoft.project.controller.grupo;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufcg.psoft.project.dto.grupo.CriteriosDesempatePutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPostRequestDTO;
import com.ufcg.psoft.project.dto.grupo.GrupoPutRequestDTO;
import com.ufcg.psoft.project.dto.grupo.RegraPontuacaoPostPutRequestDTO;
import com.ufcg.psoft.project.dto.palpite.RegrasPalpitesRequestDTO;
import com.ufcg.psoft.project.service.grupo.GrupoService;
import com.ufcg.psoft.project.service.grupo.desempate.CriterioDesempateService;
import com.ufcg.psoft.project.service.grupo.participante.GrupoParticipanteService;
import com.ufcg.psoft.project.service.grupo.pontuacao.RegraPontuacaoService;
import com.ufcg.psoft.project.service.pontuacao.PontuacaoService;
import com.ufcg.psoft.project.service.premium.RequisicaoAutenticadaEvent;
import com.ufcg.psoft.project.service.ranking.RankingService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(
    value = "/grupos",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GrupoController {
    @Autowired
    GrupoService grupoService;
    @Autowired
    private PontuacaoService pontuacaoService;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("")
    public ResponseEntity<?> criarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @RequestBody @Valid GrupoPostRequestDTO grupoPostRequestDto) {
        var resultado = grupoService.criar(usuarioId, codigoUsuario, grupoPostRequestDto);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> recuperarGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        var resultado = grupoService.recuperar(usuarioId, codigoUsuario, id);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @GetMapping("")
    public ResponseEntity<?> listarGrupos(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario
        ) {
        var resultado = grupoService.listar(usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarGrupo(
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long id) {
        grupoService.remover(usuarioId, codigoUsuario, id);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

	@PutMapping("/{grupoId}/regras-palpites")
    public ResponseEntity<?> configurarRegrasPalpites(
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

    @GetMapping("/{grupoId}/pontuacoes")
    public ResponseEntity<?> listarPontuacoesDoGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long grupoId) {
        var resultado = pontuacaoService.listarPontuacoesParticipantesDoGrupo(grupoId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }

    @GetMapping("{grupoId}/ranking")
    public ResponseEntity<?> rankingDoGrupo(
            @RequestParam Long usuarioId,
            @RequestParam String codigoUsuario,
            @PathVariable Long grupoId) {
        var resultado = rankingService.rankingDoGrupo(grupoId, usuarioId, codigoUsuario);
        eventPublisher.publishEvent(new RequisicaoAutenticadaEvent(usuarioId));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultado);
    }
}