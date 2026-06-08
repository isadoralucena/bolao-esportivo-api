package com.ufcg.psoft.project.dto.grupo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufcg.psoft.project.dto.usuario.UsuarioResponseDTO;
import com.ufcg.psoft.project.dto.campeonato.CampeonatoResponseDTO;
import com.ufcg.psoft.project.model.Grupo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoResponseDTO {
    private Long id;

    @JsonProperty("nome")
	private String nome;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("campeonato")
    private CampeonatoResponseDTO campeonato;

    @JsonProperty("organizador")
    private UsuarioResponseDTO organizador;

    public GrupoResponseDTO(Grupo grupo) {
        this.id = grupo.getId();
        this.nome = grupo.getNome();
        this.descricao = grupo.getDescricao();
        this.campeonato = new CampeonatoResponseDTO(grupo.getCampeonato());
        this.organizador = new UsuarioResponseDTO(grupo.getOrganizador());
    }
}