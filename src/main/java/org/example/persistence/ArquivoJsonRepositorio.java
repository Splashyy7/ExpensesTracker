package org.example.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.ConfiguracaoFinanceira;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void salvar(DadosPersistidos dados) throws IOException {
        Path diretorio = arquivo.getParent();
        if (diretorio != null) {
            Files.createDirectories(diretorio);
        }

        ArquivoDto dto = ArquivoDto.de(dados);
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
    public DadosPersistidos carregar() throws IOException {
        if (!Files.exists(arquivo)) {
            return new DadosPersistidos(List.of(), 1, new ConfiguracaoFinanceira());
        }

        try (Reader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            ArquivoDto dto = gson.fromJson(reader, ArquivoDto.class);
            if (dto == null || dto.despesas == null) {
                return new DadosPersistidos(List.of(), 1, new ConfiguracaoFinanceira());
            }

            List<Despesa> despesas = new ArrayList<>();
            for (DespesaDto item : dto.despesas) {
                despesas.add(item.paraDespesa());
            }
            long proximoId = dto.proximoId > 0 ? dto.proximoId : calcularProximoId(despesas);
            ConfiguracaoFinanceira config = dto.configuracao != null
                    ? dto.configuracao.paraConfiguracao()
                    : new ConfiguracaoFinanceira();
            return new DadosPersistidos(despesas, proximoId, config);
        }
    }

    public Path getArquivo() {
        return arquivo;
    }

    public Path getDiretorioBackup() {
        Path parent = arquivo.getParent();
        return parent != null ? parent.resolve("backups") : Path.of("backups");
    }

    private static long calcularProximoId(List<Despesa> despesas) {
        return despesas.stream()
                .mapToLong(Despesa::getId)
                .max()
                .orElse(0) + 1;
    }

    private record ArquivoDto(long proximoId, List<DespesaDto> despesas, ConfigDto configuracao) {

        static ArquivoDto de(DadosPersistidos dados) {
            ConfigDto configDto = ConfigDto.de(dados.configuracao());
            return new ArquivoDto(
                    dados.proximoId(),
                    dados.despesas().stream().map(DespesaDto::de).toList(),
                    configDto
            );
        }
    }

    private record ConfigDto(String orcamentoMensalPadrao, Map<String, String> limitesPorCategoria) {

        static ConfigDto de(ConfiguracaoFinanceira config) {
            Map<String, String> limites = new HashMap<>();
            config.getLimitesPorCategoria().forEach((k, v) -> limites.put(k, v.toPlainString()));
            String orcamento = config.getOrcamentoMensalPadrao()
                    .map(BigDecimal::toPlainString)
                    .orElse(null);
            return new ConfigDto(orcamento, limites);
        }

        ConfiguracaoFinanceira paraConfiguracao() {
            ConfiguracaoFinanceira config = new ConfiguracaoFinanceira();
            if (orcamentoMensalPadrao != null && !orcamentoMensalPadrao.isBlank()) {
                config.setOrcamentoMensalPadrao(new BigDecimal(orcamentoMensalPadrao));
            }
            if (limitesPorCategoria != null) {
                limitesPorCategoria.forEach((k, v) ->
                        config.definirLimiteCategoria(k, new BigDecimal(v)));
            }
            return config;
        }
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
