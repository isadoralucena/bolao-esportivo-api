package com.ufcg.psoft.project.config;
import com.ufcg.psoft.project.model.Usuario;
import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.premium.ContadorRequisicoes;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;

import java.io.IOException;
import java.util.Optional;

@Setter
public class ContadorRequisicoesFilter implements Filter {

    private ContadorRequisicoes contadorRequisicoes;

    private UsuarioRepository usuarioRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        String codigo = httpRequest.getParameter("codigo");
        if (codigo == null || codigo.isEmpty()) {
            codigo = httpRequest.getParameter("codigoAcesso");
        }

        if (codigo != null && !codigo.isEmpty()) {
            Optional<Usuario> usuario = usuarioRepository.findByCodigoIgnoreCase(codigo);
            if (usuario.isPresent()) {
                contadorRequisicoes.incrementar(usuario.get().getId());
            }
        }

        chain.doFilter(request, response);
    }
}
