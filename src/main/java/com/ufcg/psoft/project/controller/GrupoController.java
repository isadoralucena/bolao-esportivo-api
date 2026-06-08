package com.ufcg.psoft.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

@RestController
@RequestMapping(
value = "/grupos",
produces = MediaType.APPLICATION_JSON_VALUE
)
public class GrupoController {
    
}