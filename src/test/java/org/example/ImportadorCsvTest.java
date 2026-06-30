package org.example;

import org.example.exportacao.ImportadorCsv;
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

class ImportadorCsvTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve importar despesas de CSV válido")
    void deveImportarCsv() throws IOException {
        Path arquivo = tempDir.resolve("import.csv");
        Files.writeString(arquivo, """
                id,data,descricao,valor,categoria
                1,2025-06-01,Uber,25.50,Transporte
                2,2025-06-02,"Almoço, especial",35.00,Alimentação
                """);

        List<Despesa> despesas = new ImportadorCsv().lerDespesas(arquivo);
        assertEquals(2, despesas.size());
        assertEquals("Uber", despesas.get(0).getDescricao());
        assertEquals(new BigDecimal("25.50"), despesas.get(0).getValor());
        assertEquals("Almoço, especial", despesas.get(1).getDescricao());
    }

    @Test
    @DisplayName("Deve dividir campos com aspas corretamente")
    void deveDividirCamposComAspas() {
        List<String> campos = ImportadorCsv.dividirCampos("1,2025-01-01,\"texto, com virgula\",10.00,Cat");
        assertEquals(5, campos.size());
        assertEquals("texto, com virgula", campos.get(2));
    }
}
