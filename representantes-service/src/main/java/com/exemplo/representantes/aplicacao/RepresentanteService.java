package com.exemplo.representantes.aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exemplo.representantes.dominio.Representante;
import com.exemplo.representantes.dominio.RepresentanteJaCadastradoException;
import com.exemplo.representantes.dominio.RepresentanteNaoEncontradoException;
import com.exemplo.representantes.dominio.RepresentanteRepository;
import com.exemplo.representantes.dominio.Cpf;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class RepresentanteService {
    private final RepresentanteRepository repository;
    private final Counter representantesCadastrados;

    public RepresentanteService(RepresentanteRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        // Métrica de negócio exposta em /actuator/prometheus como representantes_cadastrados_total
        this.representantesCadastrados = Counter.builder("representantes.cadastrados")
                .description("Total de representantes cadastrados")
                .register(meterRegistry);
    }

    public Representante cadastrar(Representante representante) {
        if (representante == null) {
            throw new IllegalArgumentException("Representante é obrigatório");
        }
        String cpf = Cpf.normalizar(representante.cpf());
        if (representante.nome() == null || representante.nome().isBlank()) {
            throw new IllegalArgumentException("Nome do representante é obrigatório");
        }
        if (repository.existePorCpf(cpf)) {
            throw new RepresentanteJaCadastradoException(cpf);
        }
        Representante salvo = repository.salvar(new Representante(cpf, representante.nome().trim()));
        representantesCadastrados.increment();
        return salvo;
    }

    public Representante buscarPorCpf(String cpf) {
        String normalizado = Cpf.normalizar(cpf);
        return repository.buscarPorCpf(normalizado)
                .orElseThrow(() -> new RepresentanteNaoEncontradoException(normalizado));
    }

    public List<Representante> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome para busca é obrigatório");
        }
        return repository.buscarPorNome(nome.trim());
    }

    public List<Representante> listarTodos() {
        return repository.listarTodos();
    }
}
