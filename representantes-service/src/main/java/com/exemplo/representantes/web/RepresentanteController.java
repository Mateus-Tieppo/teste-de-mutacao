package com.exemplo.representantes.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exemplo.representantes.aplicacao.RepresentanteService;
import com.exemplo.representantes.dominio.Representante;

@RestController
@RequestMapping("/representantes")
public class RepresentanteController {
    private final RepresentanteService service;

    public RepresentanteController(RepresentanteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Representante> cadastrar(@RequestBody Representante representante) {
        Representante salvo = service.cadastrar(representante);
        return ResponseEntity.created(URI.create("/representantes/" + salvo.cpf())).body(salvo);
    }

    /** GET /representantes lista todos; GET /representantes?nome=x consulta pelo nome. */
    @GetMapping
    public List<Representante> listar(@RequestParam(required = false) String nome) {
        return nome == null ? service.listarTodos() : service.buscarPorNome(nome);
    }

    @GetMapping("/{cpf}")
    public Representante buscarPorCpf(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf);
    }
}
