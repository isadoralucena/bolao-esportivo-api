package com.ufcg.psoft.project.model.estadopartida;

import com.ufcg.psoft.project.model.Grupo;
import com.ufcg.psoft.project.model.JanelaDePalpites;
import com.ufcg.psoft.project.model.Partida;
import com.ufcg.psoft.project.model.PartidaStatus;

import java.time.LocalDateTime;

public interface EstadoPartida {
    boolean estaAbertaParaPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual);
    void validarCriacaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual);
    void validarEdicaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual);
    void validarExclusaoPalpite(Partida partida, JanelaDePalpites janela, LocalDateTime horaAtual);
    void validarConsolidacao();
    PartidaStatus statusEfetivoParaGrupo(Partida partida, Grupo grupo, LocalDateTime agora);
}