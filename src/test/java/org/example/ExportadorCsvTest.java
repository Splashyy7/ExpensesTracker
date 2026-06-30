package org.example;

import org.example.exportacao.ExportadorCsv;
import org.example.model.Despesa;
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

class ExportadorCsvTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve exportar despesas em formato CSV válido")
    void deveExportarCsv() throws IOException {
        Path destino = tempDir.resolve("export.csv");
        List<Despesa> despesas = List.of(
                new Despesa(1, "Uber, especial", new BigDecimal("25.50"), "Transporte",
                        LocalDate.of(2025, 6, 1))
        );

        new ExportadorCsv().exportar(despesas, destino);

        String conteudo = Files.readString(destino);
        assertTrue(conteudo.startsWith("id,data,descricao,valor,categoria"));
        assertTrue(conteudo.contains("\"Uber, especial\""));
        assertTrue(conteudo.contains("25.50"));
    }
}
