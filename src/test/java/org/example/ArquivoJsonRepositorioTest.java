package org.example;

import org.example.model.Despesa;
import org.example.persistence.ArquivoJsonRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArquivoJsonRepositorioTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve salvar e carregar despesas corretamente")
    void deveSalvarECarregar() throws IOException {
        Path arquivo = tempDir.resolve("despesas.json");
        ArquivoJsonRepositorio repo = new ArquivoJsonRepositorio(arquivo);

        List<Despesa> despesas = List.of(
                new Despesa(1, "Uber", new BigDecimal("25.50"), "Transporte", LocalDate.of(2025, 6, 1)),
                new Despesa(2, "Almoço", new BigDecimal("35.00"), "Alimentação", LocalDate.of(2025, 6, 2))
        );

        repo.salvar(despesas, 3);
        var carregado = repo.carregar();

        assertEquals(2, carregado.despesas().size());
        assertEquals(3, carregado.proximoId());
        assertEquals("Uber", carregado.despesas().get(0).getDescricao());
        assertEquals(new BigDecimal("25.50"), carregado.despesas().get(0).getValor());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando arquivo não existe")
    void deveRetornarVazioSeArquivoNaoExiste() throws IOException {
        Path arquivo = tempDir.resolve("inexistente.json");
        ArquivoJsonRepositorio repo = new ArquivoJsonRepositorio(arquivo);

        var carregado = repo.carregar();
        assertTrue(carregado.despesas().isEmpty());
        assertEquals(1, carregado.proximoId());
    }

    @Test
    @DisplayName("Deve criar diretórios automaticamente ao salvar")
    void deveCriarDiretorios() throws IOException {
        Path arquivo = tempDir.resolve("subdir/dados/despesas.json");
        ArquivoJsonRepositorio repo = new ArquivoJsonRepositorio(arquivo);

        repo.salvar(List.of(), 1);
        assertTrue(Files.exists(arquivo));
    }
}
