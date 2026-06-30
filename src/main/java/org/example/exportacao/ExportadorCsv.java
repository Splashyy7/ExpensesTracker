package org.example.exportacao;

import org.example.model.Despesa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exporta despesas para formato CSV compatível com planilhas.
 */
public class ExportadorCsv {

    private static final String CABECALHO = "id,data,descricao,valor,categoria";

    public void exportar(List<Despesa> despesas, Path destino) throws IOException {
        Path diretorio = destino.getParent();
        if (diretorio != null) {
            Files.createDirectories(diretorio);
        }

        StringBuilder conteudo = new StringBuilder(CABECALHO).append('\n');
        for (Despesa d : despesas) {
            conteudo.append(d.getId()).append(',')
                    .append(d.getData()).append(',')
                    .append(escapar(d.getDescricao())).append(',')
                    .append(d.getValor().toPlainString()).append(',')
                    .append(escapar(d.getCategoria())).append('\n');
        }

        Files.writeString(destino, conteudo.toString(), StandardCharsets.UTF_8);
    }

    private static String escapar(String valor) {
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
