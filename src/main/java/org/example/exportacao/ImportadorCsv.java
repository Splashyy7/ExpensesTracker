package org.example.exportacao;

import org.example.exception.ValidacaoException;
import org.example.model.Despesa;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Importa despesas a partir de arquivo CSV exportado pela aplicação.
 */
public class ImportadorCsv {

    private static final String CABECALHO_ESPERADO = "id,data,descricao,valor,categoria";

    public record ResultadoImportacao(int importadas, int ignoradas, List<String> erros) {
        public boolean temErros() {
            return !erros.isEmpty();
        }
    }

    public List<Despesa> lerDespesas(Path arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        if (linhas.isEmpty()) {
            throw new ValidacaoException("O arquivo CSV está vazio.");
        }

        int inicio = 0;
        if (linhas.get(0).trim().equalsIgnoreCase(CABECALHO_ESPERADO)) {
            inicio = 1;
        }

        List<Despesa> despesas = new ArrayList<>();
        for (int i = inicio; i < linhas.size(); i++) {
            String linha = linhas.get(i).trim();
            if (linha.isEmpty()) {
                continue;
            }
            despesas.add(parsearLinha(linha, i + 1));
        }
        return despesas;
    }

    private Despesa parsearLinha(String linha, int numeroLinha) {
        List<String> campos = dividirCampos(linha);
        if (campos.size() < 5) {
            throw new ValidacaoException("Linha " + numeroLinha + ": formato inválido (esperado 5 colunas).");
        }

        try {
            String descricao = campos.get(2);
            BigDecimal valor = new BigDecimal(campos.get(3).replace(',', '.'));
            String categoria = campos.get(4);
            LocalDate data = LocalDate.parse(campos.get(1));
            return new Despesa(descricao, valor, categoria, data);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ValidacaoException("Linha " + numeroLinha + ": dados inválidos — " + e.getMessage());
        }
    }

    public static List<String> dividirCampos(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (c == ',' && !entreAspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }
}
