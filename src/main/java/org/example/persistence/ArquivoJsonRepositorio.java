package org.example.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.Despesa;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistência em arquivo JSON com escrita atômica para evitar corrupção de dados.
 */
public class ArquivoJsonRepositorio implements RepositorioDespesas {

    private final Path arquivo;
    private final Gson gson;

    public ArquivoJsonRepositorio(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath().normalize();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public static ArquivoJsonRepositorio padrao() {
        String caminho = System.getenv("EXPENSES_DATA_FILE");
        Path path;
        if (caminho != null && !caminho.isBlank()) {
            path = Path.of(caminho);
        } else {
            String home = System.getProperty("user.home");
            path = Path.of(home, ".expenses-tracker", "despesas.json");
        }
        return new ArquivoJsonRepositorio(path);
    }

    @Override
    public void salvar(List<Despesa> despesas, long proximoId) throws IOException {
        Path diretorio = arquivo.getParent();
        if (diretorio != null) {
            Files.createDirectories(diretorio);
        }

        ArquivoDto dto = new ArquivoDto(proximoId, despesas.stream().map(DespesaDto::de).toList());
        Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");

        try (Writer writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            gson.toJson(dto, writer);
        }

        try {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public DadosCarregados carregar() throws IOException {
        if (!Files.exists(arquivo)) {
            return new DadosCarregados(List.of(), 1);
        }

        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            ArquivoDto dto = gson.fromJson(reader, ArquivoDto.class);
            if (dto == null || dto.despesas == null) {
                return new DadosCarregados(List.of(), 1);
            }

            List<Despesa> despesas = new ArrayList<>();
            for (DespesaDto item : dto.despesas) {
                despesas.add(item.paraDespesa());
            }
            long proximoId = dto.proximoId > 0 ? dto.proximoId : calcularProximoId(despesas);
            return new DadosCarregados(despesas, proximoId);
        }
    }

    public Path getArquivo() {
        return arquivo;
    }

    private static long calcularProximoId(List<Despesa> despesas) {
        return despesas.stream()
                .mapToLong(Despesa::getId)
                .max()
                .orElse(0) + 1;
    }

    private record ArquivoDto(long proximoId, List<DespesaDto> despesas) {
    }

    private record DespesaDto(long id, String descricao, String valor, String categoria, String data) {

        static DespesaDto de(Despesa despesa) {
            return new DespesaDto(
                    despesa.getId(),
                    despesa.getDescricao(),
                    despesa.getValor().toPlainString(),
                    despesa.getCategoria(),
                    despesa.getData().toString()
            );
        }

        Despesa paraDespesa() {
            return new Despesa(id, descricao, new BigDecimal(valor), categoria, LocalDate.parse(data));
        }
    }
}
