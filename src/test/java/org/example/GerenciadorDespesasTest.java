package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorDespesasTest {

    private GerenciadorDespesas gerenciador;

    @BeforeEach
    void setUp() {
        gerenciador = new GerenciadorDespesas();
    }

    @Test
    @DisplayName("Deve adicionar despesas na lista interna")
    void deveAdicionarDespesaNaLista() {
        gerenciador.adicionarDespesa("Cinema", 40.0, "Lazer");
        assertFalse(gerenciador.getDespesas().isEmpty());
        assertEquals(1, gerenciador.getDespesas().size());
        assertEquals("Cinema", gerenciador.getDespesas().get(0).getDescricao());
    }

    @Test
    @DisplayName("Deve calcular o total corretamente com várias despesas")
    void deveCalcularTotalCorretamente() {
        gerenciador.adicionarDespesa("Lanche", 10.0, "Alimentação");
        gerenciador.adicionarDespesa("Uber", 20.50, "Transporte");
        double somaReal = gerenciador.getDespesas().stream()
                .mapToDouble(Despesa::getValor)
                .sum();

        assertEquals(30.50, somaReal, 0.001);
    }

    @Test
    @DisplayName("Deve retornar total zero quando a lista estiver vazia")
    void deveRetornarZeroSeListaVazia() {
        double somaReal = gerenciador.getDespesas().stream()
                .mapToDouble(Despesa::getValor)
                .sum();

        assertEquals(0.0, somaReal);
    }
}