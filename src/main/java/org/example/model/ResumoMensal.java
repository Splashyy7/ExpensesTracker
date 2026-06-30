package org.example.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

/**
 * Resumo financeiro de um mês específico.
 */
public record ResumoMensal(
        YearMonth mes,
        int quantidadeDespesas,
        BigDecimal totalGasto,
        Map<String, BigDecimal> totaisPorCategoria,
        EstatisticasDespesas estatisticas
) {
}
