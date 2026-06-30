package org.example.service;

import org.example.model.Despesa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cria cópias de segurança do arquivo de dados.
 */
public class ServicoBackup {

    private static final DateTimeFormatter SUFIXO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public Path criarBackup(Path arquivoOrigem, Path diretorioBackup) throws IOException {
        if (!Files.exists(arquivoOrigem)) {
            throw new IOException("Arquivo de dados não encontrado: " + arquivoOrigem);
        }

        Files.createDirectories(diretorioBackup);
        String nome = arquivoOrigem.getFileName().toString();
        String backupNome = nome.replace(".json", "") + "_backup_" + LocalDateTime.now().format(SUFIXO) + ".json";
        Path destino = diretorioBackup.resolve(backupNome).normalize();

        Files.copy(arquivoOrigem, destino, StandardCopyOption.REPLACE_EXISTING);
        return destino;
    }
}
