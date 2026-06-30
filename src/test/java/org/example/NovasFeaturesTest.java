package org.example;

import org.example.model.ConfiguracaoFinanceira;
import org.example.model.Despesa;
import org.example.model.EstatisticasDespesas;
import org.example.model.ResumoMensal;
import org.example.service.GerenciadorDespesas;
import org.example.service.ServicoBackup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NovasFeaturesTest {

    private GerenciadorDespesas gerenciador;

    @BeforeEach
    void setUp() {
        gerenciador = new GerenciadorDespesas();
    }

    @Test
    @DisplayName("Deve calcular estatísticas gerais")
    void deveCalcularEstatisticas() {
        gerenciador.adicionarDespesa("A", new BigDecimal("10.00"), "Cat", LocalDate.of(2025, 6, 1));
        gerenciador.adicionarDespesa("B", new BigDecimal("30.00"), "Cat", LocalDate.of(2025, 6, 2));
        gerenciador.adicionarDespesa("C", new BigDecimal("20.00"), "Cat", LocalDate.of(2025, 6, 3));

        EstatisticasDespesas stats = gerenciador.calcularEstatisticas();
        assertEquals(3, stats.quantidade());
        assertEquals(0, new BigDecimal("20.00").compareTo(stats.media()));
        assertEquals(0, new BigDecimal("10.00").compareTo(stats.menor()));
        assertEquals(0, new BigDecimal("30.00").compareTo(stats.maior()));
    }

    @Test
    @DisplayName("Deve gerar resumo mensal")
    void deveGerarResumoMensal() {
        gerenciador.adicionarDespesa("Uber", new BigDecimal("50.00"), "Transporte", LocalDate.of(2025, 6, 10));
        gerenciador.adicionarDespesa("Almoço", new BigDecimal("25.00"), "Alimentação", LocalDate.of(2025, 6, 15));
        gerenciador.adicionarDespesa("Outro mês", new BigDecimal("100.00"), "Cat", LocalDate.of(2025, 5, 1));

        ResumoMensal resumo = gerenciador.gerarResumoMensal(YearMonth.of(2025, 6));
        assertEquals(2, resumo.quantidadeDespesas());
        assertEquals(0, new BigDecimal("75.00").compareTo(resumo.totalGasto()));
        assertEquals(2, resumo.totaisPorCategoria().size());
    }

    @Test
    @DisplayName("Deve importar despesas atribuindo novos IDs")
    void deveImportarDespesas() {
        Despesa d1 = new Despesa("Importada", new BigDecimal("15.00"), "Cat", LocalDate.of(2025, 1, 1));
        int count = gerenciador.importarDespesas(List.of(d1));

        assertEquals(1, count);
        assertEquals(1, gerenciador.getDespesas().get(0).getId());
        assertEquals("Importada", gerenciador.getDespesas().get(0).getDescricao());
    }

    @Test
    @DisplayName("Deve detectar categorias acima do limite")
    void deveVerificarLimitesCategoria() {
        gerenciador.adicionarDespesa("A", new BigDecimal("150.00"), "Lazer", LocalDate.now());
        Map<String, BigDecimal> limites = Map.of("Lazer", new BigDecimal("100.00"));

        List<String> alertas = gerenciador.verificarLimitesCategoria(limites);
        assertEquals(1, alertas.size());
        assertTrue(alertas.get(0).contains("Lazer"));
    }

    @Test
    @DisplayName("Deve criar backup do arquivo de dados")
    void deveCriarBackup(@TempDir Path tempDir) throws IOException {
        Path dados = tempDir.resolve("despesas.json");
        Files.writeString(dados, "{\"proximoId\":1,\"despesas\":[]}");

        Path backupDir = tempDir.resolve("backups");
        Path backup = new ServicoBackup().criarBackup(dados, backupDir);

        assertTrue(Files.exists(backup));
        assertTrue(backup.getFileName().toString().contains("backup"));
    }

    @Test
    @DisplayName("Configuração financeira deve armazenar limites")
    void deveConfigurarOrcamento() {
        ConfiguracaoFinanceira config = new ConfiguracaoFinanceira();
        config.setOrcamentoMensalPadrao(new BigDecimal("2000.00"));
        config.definirLimiteCategoria("Alimentação", new BigDecimal("500.00"));

        assertEquals(0, new BigDecimal("2000.00").compareTo(config.getOrcamentoMensalPadrao().orElseThrow()));
        assertEquals(0, new BigDecimal("500.00").compareTo(config.getLimiteCategoria("alimentação").orElseThrow()));
    }
}
