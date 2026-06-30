package org.example.persistence;

import org.example.model.ConfiguracaoFinanceira;
import org.example.model.Despesa;
import org.example.service.GerenciadorDespesas;

import java.io.IOException;
import java.util.List;

/**
 * Contrato para persistência de despesas entre sessões.
 */
public interface RepositorioDespesas {

    void salvar(DadosPersistidos dados) throws IOException;

    default void salvar(List<Despesa> despesas, long proximoId) throws IOException {
        salvar(new DadosPersistidos(despesas, proximoId, new ConfiguracaoFinanceira()));
    }

    DadosPersistidos carregar() throws IOException;

    record DadosPersistidos(List<Despesa> despesas, long proximoId, ConfiguracaoFinanceira configuracao) {
        public GerenciadorDespesas paraGerenciador() {
            return new GerenciadorDespesas(despesas, proximoId);
        }
    }

    /** @deprecated use {@link DadosPersistidos} */
    @Deprecated
    record DadosCarregados(List<Despesa> despesas, long proximoId) {
        public GerenciadorDespesas paraGerenciador() {
            return new GerenciadorDespesas(despesas, proximoId);
        }
    }
}
