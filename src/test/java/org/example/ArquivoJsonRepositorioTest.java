package org.example;

import org.example.model.ConfiguracaoFinanceira;
import org.example.model.Despesa;
import org.example.persistence.ArquivoJsonRepositorio;
import org.example.persistence.RepositorioDespesas.DadosPersistidos;
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

        repo.salvar(new DadosPersistidos(despesas, 3, new ConfiguracaoFinanceira()));
        var carregado = repo.carregar();

        assertEquals(2, carregado.despesas().size());
        assertEquals(3, carregado.proximoId());
        assertEquals("Uber", carregado.despesas().get(0).getDescricao());
        assertEquals(0, new BigDecimal("25.50").compareTo(carregado.despesas().get(0).getValor()));
    }

    @Test
    @DisplayName("Deve persistir configuração de orçamento")
    void devePersistirConfiguracao() throws IOException {
        Path arquivo = tempDir.resolve("despesas.json");
        ArquivoJsonRepositorio repo = new ArquivoJsonRepositorio(arquivo);

        ConfiguracaoFinanceira config = new ConfiguracaoFinanceira();
        config.setOrcamentoMensalPadrao(new BigDecimal("3000.00"));
        config.definirLimiteCategoria("Transporte", new BigDecimal("400.00"));

        repo.salvar(new DadosPersistidos(List.of(), 1, config));
        var carregado = repo.carregar();

        assertEquals(0, new BigDecimal("3000.00")
                .compareTo(carregado.configuracao().getOrcamentoMensalPadrao().orElseThrow()));
        assertEquals(0, new BigDecimal("400.00")
                .compareTo(carregado.configuracao().getLimiteCategoria("Transporte").orElseThrow()));
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

        repo.salvar(new DadosPersistidos(List.of(), 1, new ConfiguracaoFinanceira()));
        assertTrue(Files.exists(arquivo));
    }

    @Test
    @DisplayName("Deve carregar JSON legado sem campo configuracao")
    void deveCarregarJsonLegado() throws IOException {
        Path arquivo = tempDir.resolve("legado.json");
        Files.writeString(arquivo, """
                {
                  "proximoId": 2,
                  "despesas": [
                    {"id":1,"descricao":"Teste","valor":"10.00","categoria":"Cat","data":"2025-06-01"}
                  ]
                }
                """);

        var carregado = new ArquivoJsonRepositorio(arquivo).carregar();
        assertEquals(1, carregado.despesas().size());
        assertNotNull(carregado.configuracao());
    }
}
