package org.example.service;

import org.example.exception.ValidacaoException;
import org.example.model.Despesa;
import org.example.model.EstatisticasDespesas;
import org.example.model.ResumoMensal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Camada de serviço responsável pela lógica de negócio das despesas.
 * Não realiza I/O de console — retorna dados para a camada de apresentação.
 */
public class GerenciadorDespesas {

    public enum Ordenacao {
        DATA_DESC,
        DATA_ASC,
        VALOR_DESC,
        VALOR_ASC,
        CATEGORIA
    }

    private final List<Despesa> despesas;
    private long proximoId;

    public GerenciadorDespesas() {
        this.despesas = new ArrayList<>();
        this.proximoId = 1;
    }

    public GerenciadorDespesas(List<Despesa> despesasCarregadas, long proximoId) {
        this.despesas = new ArrayList<>(despesasCarregadas);
        this.proximoId = proximoId;
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    public long getProximoId() {
        return proximoId;
    }

    public Despesa adicionarDespesa(String descricao, BigDecimal valor, String categoria, LocalDate data) {
        Despesa nova = new Despesa(proximoId++, descricao, valor, categoria, data);
        despesas.add(nova);
        return nova;
    }

    public Despesa adicionarDespesa(String descricao, BigDecimal valor, String categoria) {
        return adicionarDespesa(descricao, valor, categoria, LocalDate.now());
    }

    public Optional<Despesa> buscarPorId(long id) {
        return despesas.stream()
                .filter(d -> d.getId() == id)
                .findFirst();
    }

    public boolean removerDespesa(long id) {
        return despesas.removeIf(d -> d.getId() == id);
    }

    public Despesa atualizarDespesa(long id, String descricao, BigDecimal valor, String categoria, LocalDate data) {
        Despesa despesa = buscarPorId(id)
                .orElseThrow(() -> new ValidacaoException("Despesa com ID #" + id + " não encontrada."));
        despesa.atualizar(descricao, valor, categoria, data);
        return despesa;
    }

    public BigDecimal calcularTotal() {
        return despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotalPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String cat = categoria.trim();
        return despesas.stream()
                .filter(d -> d.getCategoria().equalsIgnoreCase(cat))
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> calcularTotaisPorCategoria() {
        return despesas.stream()
                .collect(Collectors.groupingBy(
                        Despesa::getCategoria,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Despesa::getValor, BigDecimal::add)
                ));
    }

    public List<Despesa> filtrarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return List.of();
        }
        String cat = categoria.trim();
        return despesas.stream()
                .filter(d -> d.getCategoria().equalsIgnoreCase(cat))
                .collect(Collectors.toList());
    }

    public List<Despesa> filtrarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new ValidacaoException("As datas de início e fim são obrigatórias.");
        }
        if (inicio.isAfter(fim)) {
            throw new ValidacaoException("A data de início não pode ser posterior à data de fim.");
        }
        return despesas.stream()
                .filter(d -> !d.getData().isBefore(inicio) && !d.getData().isAfter(fim))
                .collect(Collectors.toList());
    }

    public List<Despesa> buscarPorDescricao(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return List.of();
        }
        String busca = termo.trim().toLowerCase();
        return despesas.stream()
                .filter(d -> d.getDescricao().toLowerCase().contains(busca))
                .collect(Collectors.toList());
    }

    public List<Despesa> listarOrdenado(Ordenacao ordenacao) {
        Comparator<Despesa> comparator = switch (ordenacao) {
            case DATA_ASC -> Comparator.comparing(Despesa::getData);
            case DATA_DESC -> Comparator.comparing(Despesa::getData).reversed();
            case VALOR_ASC -> Comparator.comparing(Despesa::getValor);
            case VALOR_DESC -> Comparator.comparing(Despesa::getValor).reversed();
            case CATEGORIA -> Comparator.comparing(Despesa::getCategoria, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Despesa::getData, Comparator.reverseOrder());
        };
        return despesas.stream().sorted(comparator).collect(Collectors.toList());
    }

    public List<String> listarCategorias() {
        return despesas.stream()
                .map(Despesa::getCategoria)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public boolean orcamentoExcedido(BigDecimal limiteMensal, LocalDate mesReferencia) {
        if (limiteMensal == null || limiteMensal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O limite do orçamento deve ser maior que zero.");
        }
        LocalDate inicio = mesReferencia.withDayOfMonth(1);
        LocalDate fim = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
        BigDecimal totalMes = filtrarPorPeriodo(inicio, fim).stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalMes.compareTo(limiteMensal) > 0;
    }

    public BigDecimal totalDoMes(LocalDate mesReferencia) {
        LocalDate inicio = mesReferencia.withDayOfMonth(1);
        LocalDate fim = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
        return filtrarPorPeriodo(inicio, fim).stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int quantidade() {
        return despesas.size();
    }

    public EstatisticasDespesas calcularEstatisticas() {
        return calcularEstatisticas(despesas);
    }

    public EstatisticasDespesas calcularEstatisticas(LocalDate inicio, LocalDate fim) {
        return calcularEstatisticas(filtrarPorPeriodo(inicio, fim));
    }

    public ResumoMensal gerarResumoMensal(YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        List<Despesa> doMes = filtrarPorPeriodo(inicio, fim);

        Map<String, BigDecimal> porCategoria = doMes.stream()
                .collect(Collectors.groupingBy(
                        Despesa::getCategoria,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Despesa::getValor, BigDecimal::add)
                ));

        EstatisticasDespesas stats = calcularEstatisticas(doMes);
        return new ResumoMensal(mes, doMes.size(), stats.total(), porCategoria, stats);
    }

    public int importarDespesas(List<Despesa> despesasImportadas) {
        int count = 0;
        for (Despesa d : despesasImportadas) {
            adicionarDespesa(d.getDescricao(), d.getValor(), d.getCategoria(), d.getData());
            count++;
        }
        return count;
    }

    public List<String> verificarLimitesCategoria(Map<String, BigDecimal> limites) {
        List<String> alertas = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : limites.entrySet()) {
            BigDecimal gasto = calcularTotalPorCategoria(entry.getKey());
            if (gasto.compareTo(entry.getValue()) > 0) {
                BigDecimal excesso = gasto.subtract(entry.getValue());
                alertas.add(String.format("%s: gasto %s excede limite %s (+%s)",
                        entry.getKey(), gasto.toPlainString(), entry.getValue().toPlainString(),
                        excesso.toPlainString()));
            }
        }
        return alertas;
    }

    private static EstatisticasDespesas calcularEstatisticas(List<Despesa> lista) {
        if (lista.isEmpty()) {
            return EstatisticasDespesas.vazia();
        }
        BigDecimal total = lista.stream().map(Despesa::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal menor = lista.stream().map(Despesa::getValor).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maior = lista.stream().map(Despesa::getValor).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return EstatisticasDespesas.de(lista.size(), total, menor, maior);
    }
}
