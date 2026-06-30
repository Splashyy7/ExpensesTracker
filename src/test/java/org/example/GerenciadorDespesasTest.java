package org.example;

import org.example.exception.ValidacaoException;
import org.example.model.Despesa;
import org.example.service.GerenciadorDespesas;
import org.example.service.GerenciadorDespesas.Ordenacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorDespesasTest {

    private GerenciadorDespesas gerenciador;

    @BeforeEach
    void setUp() {
        gerenciador = new GerenciadorDespesas();
    }

    @Test
    @DisplayName("Deve adicionar despesa e atribuir ID sequencial")
    void deveAdicionarDespesa() {
        Despesa d1 = gerenciador.adicionarDespesa("Cinema", new BigDecimal("40"), "Lazer");
        Despesa d2 = gerenciador.adicionarDespesa("Uber", new BigDecimal("20"), "Transporte");

        assertEquals(2, gerenciador.quantidade());
        assertEquals(1, d1.getId());
        assertEquals(2, d2.getId());
        assertEquals(3, gerenciador.getProximoId());
    }

    @Test
    @DisplayName("Deve calcular total corretamente")
    void deveCalcularTotal() {
        gerenciador.adicionarDespesa("Lanche", new BigDecimal("10"), "Alimentação");
        gerenciador.adicionarDespesa("Uber", new BigDecimal("20.50"), "Transporte");

        assertEquals(new BigDecimal("30.50"), gerenciador.calcularTotal());
    }

    @Test
    @DisplayName("Deve retornar zero quando lista vazia")
    void deveRetornarZeroSeVazio() {
        assertEquals(BigDecimal.ZERO, gerenciador.calcularTotal());
    }

    @Test
    @DisplayName("Deve remover despesa por ID")
    void deveRemoverDespesa() {
        Despesa d = gerenciador.adicionarDespesa("Teste", new BigDecimal("10"), "Cat");
        assertTrue(gerenciador.removerDespesa(d.getId()));
        assertEquals(0, gerenciador.quantidade());
        assertFalse(gerenciador.removerDespesa(999));
    }

    @Test
    @DisplayName("Deve atualizar despesa existente")
    void deveAtualizarDespesa() {
        Despesa d = gerenciador.adicionarDespesa("Antigo", new BigDecimal("10"), "Cat");
        gerenciador.atualizarDespesa(d.getId(), "Novo", new BigDecimal("50"), "NovaCat", LocalDate.now());

        Despesa atualizada = gerenciador.buscarPorId(d.getId()).orElseThrow();
        assertEquals("Novo", atualizada.getDescricao());
        assertEquals(0, new BigDecimal("50.00").compareTo(atualizada.getValor()));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar ID inexistente")
    void deveFalharAoAtualizarInexistente() {
        assertThrows(ValidacaoException.class,
                () -> gerenciador.atualizarDespesa(99, "X", new BigDecimal("1"), "C", LocalDate.now()));
    }

    @Test
    @DisplayName("Deve filtrar por categoria ignorando maiúsculas")
    void deveFiltrarPorCategoria() {
        gerenciador.adicionarDespesa("A", new BigDecimal("10"), "Alimentação");
        gerenciador.adicionarDespesa("B", new BigDecimal("20"), "Transporte");
        gerenciador.adicionarDespesa("C", new BigDecimal("5"), "alimentação");

        List<Despesa> filtradas = gerenciador.filtrarPorCategoria("ALIMENTAÇÃO");
        assertEquals(2, filtradas.size());
    }

    @Test
    @DisplayName("Deve filtrar por período")
    void deveFiltrarPorPeriodo() {
        gerenciador.adicionarDespesa("A", new BigDecimal("10"), "Cat",
                LocalDate.of(2025, 1, 10));
        gerenciador.adicionarDespesa("B", new BigDecimal("20"), "Cat",
                LocalDate.of(2025, 2, 15));
        gerenciador.adicionarDespesa("C", new BigDecimal("5"), "Cat",
                LocalDate.of(2025, 3, 1));

        List<Despesa> filtradas = gerenciador.filtrarPorPeriodo(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 28));
        assertEquals(2, filtradas.size());
    }

    @Test
    @DisplayName("Deve rejeitar período inválido")
    void deveRejeitarPeriodoInvalido() {
        assertThrows(ValidacaoException.class,
                () -> gerenciador.filtrarPorPeriodo(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 1, 1)));
    }

    @Test
    @DisplayName("Deve buscar por descrição parcial")
    void deveBuscarPorDescricao() {
        gerenciador.adicionarDespesa("Uber para aeroporto", new BigDecimal("50"), "Transporte");
        gerenciador.adicionarDespesa("Cinema", new BigDecimal("40"), "Lazer");

        assertEquals(1, gerenciador.buscarPorDescricao("uber").size());
    }

    @Test
    @DisplayName("Deve calcular totais por categoria")
    void deveCalcularTotaisPorCategoria() {
        gerenciador.adicionarDespesa("A", new BigDecimal("10"), "Alimentação");
        gerenciador.adicionarDespesa("B", new BigDecimal("20"), "Transporte");
        gerenciador.adicionarDespesa("C", new BigDecimal("5"), "Alimentação");

        Map<String, BigDecimal> totais = gerenciador.calcularTotaisPorCategoria();
        assertEquals(0, new BigDecimal("15.00").compareTo(totais.get("Alimentação")));
        assertEquals(0, new BigDecimal("20.00").compareTo(totais.get("Transporte")));
    }

    @Test
    @DisplayName("Deve ordenar despesas por valor decrescente")
    void deveOrdenarPorValor() {
        gerenciador.adicionarDespesa("Barato", new BigDecimal("5"), "Cat");
        gerenciador.adicionarDespesa("Caro", new BigDecimal("100"), "Cat");

        List<Despesa> ordenadas = gerenciador.listarOrdenado(Ordenacao.VALOR_DESC);
        assertEquals(0, new BigDecimal("100.00").compareTo(ordenadas.get(0).getValor()));
    }

    @Test
    @DisplayName("Deve verificar orçamento mensal")
    void deveVerificarOrcamento() {
        LocalDate mes = LocalDate.of(2025, 6, 15);
        gerenciador.adicionarDespesa("A", new BigDecimal("600"), "Cat", mes);
        gerenciador.adicionarDespesa("B", new BigDecimal("500"), "Cat", mes);

        assertTrue(gerenciador.orcamentoExcedido(new BigDecimal("1000"), mes));
        assertFalse(gerenciador.orcamentoExcedido(new BigDecimal("2000"), mes));
        assertEquals(0, new BigDecimal("1100.00").compareTo(gerenciador.totalDoMes(mes)));
    }

    @Test
    @DisplayName("Lista retornada deve ser imutável")
    void listaDeveSerImutavel() {
        gerenciador.adicionarDespesa("X", new BigDecimal("1"), "C");
        assertThrows(UnsupportedOperationException.class,
                () -> gerenciador.getDespesas().add(
                        new Despesa("Y", new BigDecimal("2"), "C")));
    }
}
