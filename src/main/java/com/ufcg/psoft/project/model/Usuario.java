package com.ufcg.psoft.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("nome")
    @Column(nullable = false)
    private String nome;

    @JsonProperty("email")
    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty("username")
    @Column(nullable = false)
    private String username;

    @JsonProperty("endereco")
    @Column(nullable = false)
    private String endereco;

    @JsonProperty("perfil")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PerfilUsuario perfil = PerfilUsuario.PADRAO;
    
    @JsonIgnore
    @Column(nullable = false)
    private String codigo;
}
