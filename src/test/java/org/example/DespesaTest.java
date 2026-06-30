package org.example;

import org.example.exception.ValidacaoException;
import org.example.model.Despesa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DespesaTest {

    @Test
    @DisplayName("Deve criar despesa com atributos corretos")
    void deveCriarDespesaCorretamente() {
        LocalDate data = LocalDate.of(2025, 6, 15);
        Despesa despesa = new Despesa(1, "Uber", new BigDecimal("20.50"), "Transporte", data);

        assertAll("Atributos da Despesa",
                () -> assertEquals(1, despesa.getId()),
                () -> assertEquals("Uber", despesa.getDescricao()),
                () -> assertEquals(new BigDecimal("20.50"), despesa.getValor()),
                () -> assertEquals("Transporte", despesa.getCategoria()),
                () -> assertEquals(data, despesa.getData())
        );
    }

    @Test
    @DisplayName("Deve usar data atual quando não informada")
    void deveUsarDataAtualPorPadrao() {
        Despesa despesa = new Despesa("Café", new BigDecimal("5.00"), "Alimentação");
        assertEquals(LocalDate.now(), despesa.getData());
    }

    @Test
    @DisplayName("Deve arredondar valor para duas casas decimais")
    void deveArredondarValor() {
        Despesa despesa = new Despesa("Item", new BigDecimal("10.999"), "Outros");
        assertEquals(new BigDecimal("11.00"), despesa.getValor());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Deve rejeitar descrição vazia")
    void deveRejeitarDescricaoVazia(String descricao) {
        assertThrows(ValidacaoException.class,
                () -> new Despesa(descricao, new BigDecimal("10"), "Cat"));
    }

    @Test
    @DisplayName("Deve rejeitar valor zero ou negativo")
    void deveRejeitarValorInvalido() {
        assertThrows(ValidacaoException.class,
                () -> new Despesa("Teste", BigDecimal.ZERO, "Cat"));
        assertThrows(ValidacaoException.class,
                () -> new Despesa("Teste", new BigDecimal("-5"), "Cat"));
    }

    @Test
    @DisplayName("Deve rejeitar data no futuro")
    void deveRejeitarDataFutura() {
        assertThrows(ValidacaoException.class,
                () -> new Despesa(1, "Teste", new BigDecimal("10"), "Cat", LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("Deve atualizar campos corretamente")
    void deveAtualizarDespesa() {
        Despesa despesa = new Despesa(1, "Antigo", new BigDecimal("10"), "Cat", LocalDate.now());
        LocalDate novaData = LocalDate.now().minusDays(5);

        despesa.atualizar("Novo", new BigDecimal("25.50"), "NovaCat", novaData);

        assertEquals("Novo", despesa.getDescricao());
        assertEquals(new BigDecimal("25.50"), despesa.getValor());
        assertEquals("NovaCat", despesa.getCategoria());
        assertEquals(novaData, despesa.getData());
    }

    @Test
    @DisplayName("equals e hashCode baseados no ID")
    void deveCompararPorId() {
        Despesa a = new Despesa(1, "A", new BigDecimal("10"), "Cat", LocalDate.now());
        Despesa b = new Despesa(1, "B", new BigDecimal("20"), "Outra", LocalDate.now());
        Despesa c = new Despesa(2, "A", new BigDecimal("10"), "Cat", LocalDate.now());

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
