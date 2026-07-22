package com.ufcg.psoft.project.config;

import com.ufcg.psoft.project.repository.UsuarioRepository;
import com.ufcg.psoft.project.service.premium.ContadorRequisicoes;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public ContadorRequisicoesFilter contadorRequisicoesFilter(
            ContadorRequisicoes contadorRequisicoes,
            UsuarioRepository usuarioRepository) {
        ContadorRequisicoesFilter filter = new ContadorRequisicoesFilter();
        filter.setContadorRequisicoes(contadorRequisicoes);
        filter.setUsuarioRepository(usuarioRepository);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<ContadorRequisicoesFilter> contadorRequisicoesFilterRegistration(
            ContadorRequisicoesFilter filter) {
        FilterRegistrationBean<ContadorRequisicoesFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("contadorRequisicoesFilter");
        registration.setOrder(1);
        return registration;
    }
}
