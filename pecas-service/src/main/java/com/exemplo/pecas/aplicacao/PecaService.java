package com.exemplo.pecas.aplicacao;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exemplo.pecas.dominio.Peca;
import com.exemplo.pecas.dominio.PecaJaCadastradaException;
import com.exemplo.pecas.dominio.PecaNaoEncontradaException;
import com.exemplo.pecas.dominio.PecaRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PecaService {
    private final PecaRepository repository;
    private final Counter pecasCadastradas;

    public PecaService(PecaRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        // Métrica de negócio exposta em /actuator/prometheus como pecas_cadastradas_total
        this.pecasCadastradas = Counter.builder("pecas.cadastradas")
                .description("Total de peças cadastradas")
                .register(meterRegistry);
    }

    public Peca cadastrar(Peca peca) {
        validar(peca);
        if (repository.existePorId(peca.id())) {
            throw new PecaJaCadastradaException(peca.id());
        }
        Peca salva = repository.salvar(new Peca(peca.id(), peca.nome().trim(), peca.descricao().trim()));
        pecasCadastradas.increment();
        return salva;
    }

    public Peca buscarPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new PecaNaoEncontradaException(id));
    }

    public List<Peca> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome para busca é obrigatório");
        }
        return repository.buscarPorNome(nome.trim());
    }

    public List<Peca> listarTodas() {
        return repository.listarTodas();
    }

    private void validar(Peca peca) {
        if (peca == null) {
            throw new IllegalArgumentException("Peça é obrigatória");
        }
        if (peca.id() == null || peca.id() <= 0) {
            throw new IllegalArgumentException("Número de identificação deve ser positivo");
        }
        if (peca.nome() == null || peca.nome().isBlank()) {
            throw new IllegalArgumentException("Nome da peça é obrigatório");
        }
        if (peca.descricao() == null || peca.descricao().isBlank()) {
            throw new IllegalArgumentException("Descrição da peça é obrigatória");
        }
    }
}
