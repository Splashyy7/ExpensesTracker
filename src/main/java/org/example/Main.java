package org.example;

import org.example.exception.ValidacaoException;
import org.example.exportacao.ExportadorCsv;
import org.example.exportacao.ImportadorCsv;
import org.example.model.ConfiguracaoFinanceira;
import org.example.model.Despesa;
import org.example.model.EstatisticasDespesas;
import org.example.model.ResumoMensal;
import org.example.persistence.ArquivoJsonRepositorio;
import org.example.persistence.RepositorioDespesas;
import org.example.persistence.RepositorioDespesas.DadosPersistidos;
import org.example.service.GerenciadorDespesas;
import org.example.service.GerenciadorDespesas.Ordenacao;
import org.example.service.ServicoBackup;
import org.example.util.EntradaSegura;
import org.example.util.Formatador;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação — interface de console (CLI).
 */
public class Main {

    private final GerenciadorDespesas gerenciador;
    private final RepositorioDespesas repositorio;
    private final ConfiguracaoFinanceira configuracao;
    private final EntradaSegura entrada;
    private final ExportadorCsv exportadorCsv;
    private final ImportadorCsv importadorCsv;
    private final ServicoBackup servicoBackup;
    private final Scanner scanner;

    public Main(GerenciadorDespesas gerenciador, RepositorioDespesas repositorio,
                ConfiguracaoFinanceira configuracao, Scanner scanner) {
        this.gerenciador = gerenciador;
        this.repositorio = repositorio;
        this.configuracao = configuracao;
        this.scanner = scanner;
        this.entrada = new EntradaSegura(scanner);
        this.exportadorCsv = new ExportadorCsv();
        this.importadorCsv = new ImportadorCsv();
        this.servicoBackup = new ServicoBackup();
    }

    public static void main(String[] args) {
        if (args.length > 0 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            exibirAjuda();
            return;
        }

        ArquivoJsonRepositorio repositorio = ArquivoJsonRepositorio.padrao();
        DadosPersistidos dados;

        try {
            dados = repositorio.carregar();
        } catch (IOException e) {
            System.err.println("Aviso: não foi possível carregar dados salvos. Iniciando com lista vazia.");
            dados = new DadosPersistidos(List.of(), 1, new ConfiguracaoFinanceira());
        }

        Scanner scanner = new Scanner(System.in);
        Main app = new Main(dados.paraGerenciador(), repositorio, dados.configuracao(), scanner);
        app.executar();
    }

    private static void exibirAjuda() {
        System.out.println("""
                Expenses Tracker — Gerenciador de Despesas CLI

                Uso:
                  mvn exec:java
                  java -jar ExpensesTracker.jar
                  docker compose run --rm expenses-tracker

                Variáveis de ambiente:
                  EXPENSES_DATA_FILE  Caminho do arquivo JSON (padrão: ~/.expenses-tracker/despesas.json)

                Opções:
                  -h, --help            Exibe esta ajuda
                """);
    }

    private void executar() {
        System.out.println("Bem-vindo ao Expenses Tracker!");
        if (repositorio instanceof ArquivoJsonRepositorio arquivoRepo) {
            System.out.println("Dados salvos em: " + arquivoRepo.getArquivo());
        }

        boolean executando = true;
        while (executando) {
            exibirMenu();
            int opcao = entrada.lerInteiro("Escolha uma opção: ");

            try {
                executando = processarOpcao(opcao);
            } catch (ValidacaoException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Erro de I/O: " + e.getMessage());
            }

            if (executando) {
                salvarDados();
            }
        }

        salvarDadosSilencioso();
        scanner.close();
        System.out.println("Até logo!");
    }

    private void exibirMenu() {
        System.out.println("\n--- CONTROLE DE DESPESAS ---");
        System.out.println(" 1. Adicionar despesa");
        System.out.println(" 2. Listar despesas");
        System.out.println(" 3. Ver total gasto");
        System.out.println(" 4. Totais por categoria");
        System.out.println(" 5. Filtrar por categoria");
        System.out.println(" 6. Filtrar por período");
        System.out.println(" 7. Buscar por descrição");
        System.out.println(" 8. Editar despesa");
        System.out.println(" 9. Remover despesa");
        System.out.println("10. Verificar orçamento mensal");
        System.out.println("11. Exportar para CSV");
        System.out.println("12. Importar de CSV");
        System.out.println("13. Resumo mensal");
        System.out.println("14. Estatísticas gerais");
        System.out.println("15. Configurar orçamento");
        System.out.println("16. Criar backup dos dados");
        System.out.println(" 0. Sair");
    }

    private boolean processarOpcao(int opcao) throws IOException {
        return switch (opcao) {
            case 1 -> { adicionarDespesa(); yield true; }
            case 2 -> { listarDespesas(); yield true; }
            case 3 -> { exibirTotal(); yield true; }
            case 4 -> { exibirTotaisPorCategoria(); yield true; }
            case 5 -> { filtrarPorCategoria(); yield true; }
            case 6 -> { filtrarPorPeriodo(); yield true; }
            case 7 -> { buscarPorDescricao(); yield true; }
            case 8 -> { editarDespesa(); yield true; }
            case 9 -> { removerDespesa(); yield true; }
            case 10 -> { verificarOrcamento(); yield true; }
            case 11 -> { exportarCsv(); yield true; }
            case 12 -> { importarCsv(); yield true; }
            case 13 -> { exibirResumoMensal(); yield true; }
            case 14 -> { exibirEstatisticas(); yield true; }
            case 15 -> { configurarOrcamento(); yield true; }
            case 16 -> { criarBackup(); yield true; }
            case 0 -> false;
            default -> {
                System.out.println("Opção inválida.");
                yield true;
            }
        };
    }

    private void adicionarDespesa() {
        String descricao = entrada.lerTexto("Descrição: ", true);
        BigDecimal valor = entrada.lerValorMonetario("Valor (R$): ");
        String categoria = entrada.lerTexto("Categoria (ex: Alimentação, Transporte): ", true);
        LocalDate data = entrada.lerData("Data (dd/MM/aaaa, vazio = hoje): ", true);

        Despesa criada = gerenciador.adicionarDespesa(descricao, valor, categoria, data);
        System.out.println("Despesa #" + criada.getId() + " adicionada com sucesso!");
        exibirAlertasCategoria(categoria);
    }

    private void exibirAlertasCategoria(String categoria) {
        configuracao.getLimiteCategoria(categoria).ifPresent(limite -> {
            BigDecimal gasto = gerenciador.calcularTotalPorCategoria(categoria);
            if (gasto.compareTo(limite) > 0) {
                System.out.printf("⚠ Categoria '%s' excedeu o limite configurado (%s)%n",
                        categoria, Formatador.moeda(limite));
            }
        });
    }

    private void listarDespesas() {
        System.out.println("\nOrdenar por: 1=Data↓ 2=Data↑ 3=Valor↓ 4=Valor↑ 5=Categoria");
        int ordem = entrada.lerInteiro("Opção [1]: ");
        Ordenacao ordenacao = mapearOrdenacao(ordem);
        exibirLista(gerenciador.listarOrdenado(ordenacao));
    }

    private Ordenacao mapearOrdenacao(int ordem) {
        return switch (ordem) {
            case 2 -> Ordenacao.DATA_ASC;
            case 3 -> Ordenacao.VALOR_DESC;
            case 4 -> Ordenacao.VALOR_ASC;
            case 5 -> Ordenacao.CATEGORIA;
            default -> Ordenacao.DATA_DESC;
        };
    }

    private void exibirLista(List<Despesa> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma despesa encontrada.");
            return;
        }
        System.out.println("\n--- Lista de Despesas (" + lista.size() + ") ---");
        lista.forEach(System.out::println);
    }

    private void exibirTotal() {
        System.out.printf("\nTotal gasto: %s%n", Formatador.moeda(gerenciador.calcularTotal()));
    }

    private void exibirTotaisPorCategoria() {
        Map<String, BigDecimal> totais = gerenciador.calcularTotaisPorCategoria();
        if (totais.isEmpty()) {
            System.out.println("Nenhuma despesa cadastrada.");
            return;
        }
        System.out.println("\n--- Totais por Categoria ---");
        totais.forEach((cat, total) -> {
            System.out.printf("  %-20s %s", cat + ":", Formatador.moeda(total));
            configuracao.getLimiteCategoria(cat).ifPresent(limite -> {
                if (total.compareTo(limite) > 0) {
                    System.out.print(" ⚠");
                }
            });
            System.out.println();
        });
        System.out.printf("\nTotal geral: %s%n", Formatador.moeda(gerenciador.calcularTotal()));
    }

    private void filtrarPorCategoria() {
        List<String> categorias = gerenciador.listarCategorias();
        if (categorias.isEmpty()) {
            System.out.println("Nenhuma categoria cadastrada.");
            return;
        }
        System.out.println("Categorias: " + String.join(", ", categorias));
        String categoria = entrada.lerTexto("Categoria: ", true);
        exibirLista(gerenciador.filtrarPorCategoria(categoria));
    }

    private void filtrarPorPeriodo() {
        LocalDate inicio = entrada.lerData("Data início (dd/MM/aaaa): ", false);
        LocalDate fim = entrada.lerData("Data fim (dd/MM/aaaa): ", false);
        exibirLista(gerenciador.filtrarPorPeriodo(inicio, fim));
    }

    private void buscarPorDescricao() {
        String termo = entrada.lerTexto("Termo de busca: ", true);
        exibirLista(gerenciador.buscarPorDescricao(termo));
    }

    private void editarDespesa() {
        long id = entrada.lerId("ID da despesa: ");
        if (gerenciador.buscarPorId(id).isEmpty()) {
            System.out.println("Despesa não encontrada.");
            return;
        }

        String descricao = entrada.lerTexto("Nova descrição: ", true);
        BigDecimal valor = entrada.lerValorMonetario("Novo valor (R$): ");
        String categoria = entrada.lerTexto("Nova categoria: ", true);
        LocalDate data = entrada.lerData("Nova data (dd/MM/aaaa): ", false);

        gerenciador.atualizarDespesa(id, descricao, valor, categoria, data);
        System.out.println("Despesa #" + id + " atualizada com sucesso!");
    }

    private void removerDespesa() {
        long id = entrada.lerId("ID da despesa a remover: ");
        if (gerenciador.removerDespesa(id)) {
            System.out.println("Despesa #" + id + " removida.");
        } else {
            System.out.println("Despesa não encontrada.");
        }
    }

    private void verificarOrcamento() {
        BigDecimal limite = configuracao.getOrcamentoMensalPadrao()
                .orElseGet(() -> entrada.lerValorMonetario("Limite mensal (R$): "));
        LocalDate mes = entrada.lerData("Mês de referência (dd/MM/aaaa, vazio = mês atual): ", true);
        YearMonth referencia = YearMonth.from(mes);

        BigDecimal total = gerenciador.totalDoMes(mes);
        boolean excedido = gerenciador.orcamentoExcedido(limite, mes);

        System.out.printf("\nMês: %s%n", referencia);
        System.out.printf("Total gasto: %s%n", Formatador.moeda(total));
        System.out.printf("Limite: %s%n", Formatador.moeda(limite));

        if (excedido) {
            BigDecimal excesso = total.subtract(limite);
            System.out.printf("⚠ Orçamento EXCEDIDO em %s%n", Formatador.moeda(excesso));
        } else {
            BigDecimal restante = limite.subtract(total);
            System.out.printf("✓ Dentro do orçamento. Restante: %s%n", Formatador.moeda(restante));
        }
    }

    private void exportarCsv() throws IOException {
        List<Despesa> despesas = gerenciador.listarOrdenado(Ordenacao.DATA_DESC);
        if (despesas.isEmpty()) {
            System.out.println("Nenhuma despesa para exportar.");
            return;
        }

        String caminho = entrada.lerTexto("Caminho do arquivo CSV (vazio = ./despesas_export.csv): ", false);
        Path destino = caminho.isBlank()
                ? Path.of("despesas_export.csv").toAbsolutePath().normalize()
                : Path.of(caminho).toAbsolutePath().normalize();

        exportadorCsv.exportar(despesas, destino);
        System.out.println("Exportado para: " + destino);
    }

    private void importarCsv() throws IOException {
        String caminho = entrada.lerTexto("Caminho do arquivo CSV: ", true);
        Path origem = Path.of(caminho).toAbsolutePath().normalize();

        List<Despesa> despesas = importadorCsv.lerDespesas(origem);
        int importadas = gerenciador.importarDespesas(despesas);
        System.out.printf("%d despesa(s) importada(s) com sucesso.%n", importadas);
    }

    private void exibirResumoMensal() {
        LocalDate mes = entrada.lerData("Mês (dd/MM/aaaa, vazio = atual): ", true);
        ResumoMensal resumo = gerenciador.gerarResumoMensal(YearMonth.from(mes));

        System.out.printf("\n--- Resumo de %s ---%n", resumo.mes());
        System.out.printf("Despesas: %d%n", resumo.quantidadeDespesas());
        System.out.printf("Total: %s%n", Formatador.moeda(resumo.totalGasto()));

        if (!resumo.totaisPorCategoria().isEmpty()) {
            System.out.println("\nPor categoria:");
            resumo.totaisPorCategoria().forEach((cat, total) ->
                    System.out.printf("  %-20s %s%n", cat + ":", Formatador.moeda(total)));
        }

        EstatisticasDespesas stats = resumo.estatisticas();
        if (stats.quantidade() > 0) {
            System.out.printf("\nMédia: %s | Menor: %s | Maior: %s%n",
                    Formatador.moeda(stats.media()),
                    Formatador.moeda(stats.menor()),
                    Formatador.moeda(stats.maior()));
        }
    }

    private void exibirEstatisticas() {
        System.out.println("\n1. Geral  2. Por período");
        int tipo = entrada.lerInteiro("Opção [1]: ");

        EstatisticasDespesas stats = tipo == 2
                ? gerenciador.calcularEstatisticas(
                entrada.lerData("Data início: ", false),
                entrada.lerData("Data fim: ", false))
                : gerenciador.calcularEstatisticas();

        if (stats.quantidade() == 0) {
            System.out.println("Sem dados para calcular estatísticas.");
            return;
        }

        System.out.printf("\n--- Estatísticas ---%n");
        System.out.printf("Quantidade: %d%n", stats.quantidade());
        System.out.printf("Total:      %s%n", Formatador.moeda(stats.total()));
        System.out.printf("Média:      %s%n", Formatador.moeda(stats.media()));
        System.out.printf("Menor:      %s%n", Formatador.moeda(stats.menor()));
        System.out.printf("Maior:      %s%n", Formatador.moeda(stats.maior()));
    }

    private void configurarOrcamento() {
        System.out.println("\n1. Definir orçamento mensal padrão");
        System.out.println("2. Definir limite por categoria");
        System.out.println("3. Ver configuração atual");
        int opcao = entrada.lerInteiro("Opção: ");

        switch (opcao) {
            case 1 -> {
                BigDecimal limite = entrada.lerValorMonetario("Orçamento mensal (R$): ");
                configuracao.setOrcamentoMensalPadrao(limite);
                System.out.println("Orçamento mensal definido: " + Formatador.moeda(limite));
            }
            case 2 -> {
                String categoria = entrada.lerTexto("Categoria: ", true);
                BigDecimal limite = entrada.lerValorMonetario("Limite (R$): ");
                configuracao.definirLimiteCategoria(categoria, limite);
                System.out.printf("Limite de '%s' definido: %s%n", categoria, Formatador.moeda(limite));
            }
            case 3 -> {
                System.out.println("\n--- Configuração ---");
                configuracao.getOrcamentoMensalPadrao()
                        .ifPresentOrElse(
                                l -> System.out.printf("Orçamento mensal: %s%n", Formatador.moeda(l)),
                                () -> System.out.println("Orçamento mensal: não definido"));
                Map<String, BigDecimal> limites = configuracao.getLimitesPorCategoria();
                if (limites.isEmpty()) {
                    System.out.println("Limites por categoria: nenhum");
                } else {
                    System.out.println("Limites por categoria:");
                    limites.forEach((cat, lim) ->
                            System.out.printf("  %-20s %s%n", cat + ":", Formatador.moeda(lim)));
                }
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private void criarBackup() throws IOException {
        if (!(repositorio instanceof ArquivoJsonRepositorio arquivoRepo)) {
            System.out.println("Backup disponível apenas com persistência em arquivo.");
            return;
        }

        salvarDados();
        Path backup = servicoBackup.criarBackup(arquivoRepo.getArquivo(), arquivoRepo.getDiretorioBackup());
        System.out.println("Backup criado: " + backup);
    }

    private void salvarDados() {
        try {
            repositorio.salvar(new DadosPersistidos(
                    gerenciador.getDespesas(),
                    gerenciador.getProximoId(),
                    configuracao));
        } catch (IOException e) {
            System.out.println("Aviso: falha ao salvar dados — " + e.getMessage());
        }
    }

    private void salvarDadosSilencioso() {
        try {
            repositorio.salvar(new DadosPersistidos(
                    gerenciador.getDespesas(),
                    gerenciador.getProximoId(),
                    configuracao));
        } catch (IOException ignored) {
            System.err.println("Erro ao salvar dados ao encerrar.");
        }
    }
}
