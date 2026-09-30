package com.exemplo.clientes.web;

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

import com.exemplo.clientes.aplicacao.ClienteService;
import com.exemplo.clientes.dominio.Cliente;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Cliente> cadastrar(@RequestBody Cliente cliente) {
        Cliente salvo = service.cadastrar(cliente);
        return ResponseEntity.created(URI.create("/clientes/" + salvo.cpf())).body(salvo);
    }

    /** GET /clientes lista todos; GET /clientes?nome=x consulta pelo nome. */
    @GetMapping
    public List<Cliente> listar(@RequestParam(required = false) String nome) {
        return nome == null ? service.listarTodos() : service.buscarPorNome(nome);
    }

    @GetMapping("/{cpf}")
    public Cliente buscarPorCpf(@PathVariable String cpf) {
        return service.buscarPorCpf(cpf);
    }
}
