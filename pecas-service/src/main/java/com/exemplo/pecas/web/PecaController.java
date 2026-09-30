package com.exemplo.pecas.web;

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

import com.exemplo.pecas.aplicacao.PecaService;
import com.exemplo.pecas.dominio.Peca;

@RestController
@RequestMapping("/pecas")
public class PecaController {
    private final PecaService service;

    public PecaController(PecaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Peca> cadastrar(@RequestBody Peca peca) {
        Peca salva = service.cadastrar(peca);
        return ResponseEntity.created(URI.create("/pecas/" + salva.id())).body(salva);
    }

    /** GET /pecas lista todas; GET /pecas?nome=x consulta pelo nome. */
    @GetMapping
    public List<Peca> listar(@RequestParam(required = false) String nome) {
        return nome == null ? service.listarTodas() : service.buscarPorNome(nome);
    }

    @GetMapping("/{id}")
    public Peca buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}
