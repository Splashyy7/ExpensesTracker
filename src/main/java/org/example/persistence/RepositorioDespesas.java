package org.example.persistence;

import org.example.model.Despesa;
import org.example.service.GerenciadorDespesas;

import java.io.IOException;
import java.util.List;

/**
 * Contrato para persistência de despesas entre sessões.
 */
public interface RepositorioDespesas {

    void salvar(List<Despesa> despesas, long proximoId) throws IOException;

    DadosCarregados carregar() throws IOException;

    record DadosCarregados(List<Despesa> despesas, long proximoId) {
        public GerenciadorDespesas paraGerenciador() {
            return new GerenciadorDespesas(despesas, proximoId);
        }
    }
}
