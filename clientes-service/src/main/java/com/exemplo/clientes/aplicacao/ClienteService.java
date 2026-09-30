package com.exemplo.clientes.aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exemplo.clientes.dominio.Cliente;
import com.exemplo.clientes.dominio.ClienteJaCadastradoException;
import com.exemplo.clientes.dominio.ClienteNaoEncontradoException;
import com.exemplo.clientes.dominio.ClienteRepository;
import com.exemplo.clientes.dominio.Cpf;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final Counter clientesCadastrados;

    public ClienteService(ClienteRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        // Métrica de negócio exposta em /actuator/prometheus como clientes_cadastrados_total
        this.clientesCadastrados = Counter.builder("clientes.cadastrados")
                .description("Total de clientes cadastrados")
                .register(meterRegistry);
    }

    public Cliente cadastrar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente é obrigatório");
        }
        String cpf = Cpf.normalizar(cliente.cpf());
        if (cliente.nome() == null || cliente.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }
        if (repository.existePorCpf(cpf)) {
            throw new ClienteJaCadastradoException(cpf);
        }
        Cliente salvo = repository.salvar(new Cliente(cpf, cliente.nome().trim()));
        clientesCadastrados.increment();
        return salvo;
    }

    public Cliente buscarPorCpf(String cpf) {
        String normalizado = Cpf.normalizar(cpf);
        return repository.buscarPorCpf(normalizado)
                .orElseThrow(() -> new ClienteNaoEncontradoException(normalizado));
    }

    public List<Cliente> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome para busca é obrigatório");
        }
        return repository.buscarPorNome(nome.trim());
    }

    public List<Cliente> listarTodos() {
        return repository.listarTodos();
    }
}
