package com.ufcg.psoft.project.service.premium;

/**
 * Evento publicado quando uma requisição autenticada por um usuário é recebida.
 * O listener incrementa o contador de requisições do usuário.
 *
 * @param usuarioId ID do usuário autenticado
 */
public record RequisicaoAutenticadaEvent(Long usuarioId) {
}
