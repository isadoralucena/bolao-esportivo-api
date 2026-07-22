package com.ufcg.psoft.project.service.premium;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ContadorRequisicoes {

    private final Map<Long, AtomicLong> contadores = new ConcurrentHashMap<>();

    public void incrementar(Long usuarioId) {
        contadores.computeIfAbsent(usuarioId, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long getContagem(Long usuarioId) {
        AtomicLong contador = contadores.get(usuarioId);
        return contador != null ? contador.get() : 0;
    }

    public void resetar(Long usuarioId) {
        contadores.remove(usuarioId);
    }

    public void resetarTodos() {
        contadores.clear();
    }
}
