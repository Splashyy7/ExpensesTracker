package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DespesaTest {

    @Test
    @DisplayName("Deve criar uma despesa com data atual e atributos corretos")
    void deveCriarDespesaCorretamente() {
        String descricao = "Uber";
        double valor = 20.50;
        String categoria = "Transporte";

        Despesa despesa = new Despesa(descricao, valor, categoria);

        assertAll("Atributos da Despesa",
                () -> assertEquals(descricao, despesa.getDescricao()),
                () -> assertEquals(valor, despesa.getValor()),
                () -> assertEquals(categoria, despesa.getCategoria()),
                () -> assertEquals(LocalDate.now(), despesa.getData(), "A data deve ser a de hoje")
        );
    }
}
