// A página é servida pelo próprio Gateway, então as URLs relativas
// (/pecas, /clientes, /representantes) passam sempre pelo Gateway.
document.getElementById('origem').textContent = window.location.origin;

const colunas = {
    pecas: [['id', 'Nº'], ['nome', 'Nome'], ['descricao', 'Descrição']],
    clientes: [['cpf', 'CPF'], ['nome', 'Nome']],
    representantes: [['cpf', 'CPF'], ['nome', 'Nome']],
};

// Troca de abas
document.querySelectorAll('.aba').forEach((aba) => {
    aba.addEventListener('click', () => {
        document.querySelectorAll('.aba, .painel').forEach((el) => el.classList.remove('ativa', 'ativo'));
        aba.classList.add('ativa');
        document.getElementById(aba.dataset.aba).classList.add('ativo');
    });
});

// Formulários: cadastrar, consultar por id/CPF, consultar por nome
document.querySelectorAll('form[data-recurso]').forEach((form) => {
    form.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        const recurso = form.dataset.recurso;
        const dados = Object.fromEntries(new FormData(form));

        if (form.dataset.acao === 'cadastrar') {
            if (dados.id) dados.id = Number(dados.id);
            const ok = await chamar(recurso, 'POST', `/${recurso}`, dados);
            if (ok) form.reset();
        } else if (form.dataset.acao === 'porChave') {
            await chamar(recurso, 'GET', `/${recurso}/${encodeURIComponent(dados.chave.trim())}`);
        } else {
            await chamar(recurso, 'GET', `/${recurso}?nome=${encodeURIComponent(dados.nome.trim())}`);
        }
    });
});

// Botões "Listar todos"
document.querySelectorAll('button[data-acao="listar"]').forEach((botao) => {
    botao.addEventListener('click', () => chamar(botao.dataset.recurso, 'GET', `/${botao.dataset.recurso}`));
});

async function chamar(recurso, metodo, url, corpo) {
    const erro = document.getElementById('erro');
    const tabela = document.getElementById('tabela');
    erro.hidden = true;
    tabela.replaceChildren();

    let resposta;
    try {
        resposta = await fetch(url, {
            method: metodo,
            headers: corpo ? { 'Content-Type': 'application/json' } : {},
            body: corpo ? JSON.stringify(corpo) : undefined,
        });
    } catch (e) {
        mostrarRequisicao(metodo, url, 'falha de rede');
        mostrarErro('Não foi possível falar com o Gateway: ' + e.message);
        return false;
    }

    mostrarRequisicao(metodo, url, resposta.status);
    const texto = await resposta.text();
    const json = texto ? tentarJson(texto) : null;

    if (!resposta.ok) {
        // Os serviços devolvem ProblemDetail (campo "detail"); o Gateway devolve "error"
        mostrarErro((json && (json.detail || json.error)) || texto || resposta.statusText);
        return false;
    }
    const registros = Array.isArray(json) ? json : [json];
    tabela.append(montarTabela(colunas[recurso], registros));
    return true;
}

function mostrarRequisicao(metodo, url, status) {
    const classe = typeof status === 'number' && status < 400 ? 'sucesso' : 'falha';
    document.getElementById('requisicao').innerHTML =
        `<code>${metodo} ${escapar(url)}</code> → <span class="${classe}">${status}</span>`;
}

function mostrarErro(mensagem) {
    const erro = document.getElementById('erro');
    erro.textContent = mensagem;
    erro.hidden = false;
}

function montarTabela(cols, registros) {
    if (registros.length === 0) {
        const vazio = document.createElement('p');
        vazio.textContent = 'Nenhum registro encontrado.';
        return vazio;
    }
    const tabela = document.createElement('table');
    const cabecalho = tabela.createTHead().insertRow();
    cols.forEach(([, titulo]) => {
        const th = document.createElement('th');
        th.textContent = titulo;
        cabecalho.append(th);
    });
    const corpo = tabela.createTBody();
    registros.forEach((registro) => {
        const linha = corpo.insertRow();
        cols.forEach(([campo]) => { linha.insertCell().textContent = registro[campo] ?? ''; });
    });
    return tabela;
}

function tentarJson(texto) {
    try { return JSON.parse(texto); } catch { return null; }
}

function escapar(texto) {
    return texto.replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);
}
