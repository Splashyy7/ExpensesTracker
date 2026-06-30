package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Métricas agregadas sobre um conjunto de despesas.
 */
public record EstatisticasDespesas(
        int quantidade,
        BigDecimal total,
        BigDecimal media,
        BigDecimal menor,
        BigDecimal maior
) {
    public static EstatisticasDespesas vazia() {
        return new EstatisticasDespesas(0, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static EstatisticasDespesas de(int quantidade, BigDecimal total,
                                          BigDecimal menor, BigDecimal maior) {
        if (quantidade == 0) {
            return vazia();
        }
        BigDecimal media = total.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
        return new EstatisticasDespesas(quantidade, total, media, menor, maior);
    }

    public Optional<BigDecimal> menorOpcional() {
        return quantidade > 0 ? Optional.of(menor) : Optional.empty();
    }

    public Optional<BigDecimal> maiorOpcional() {
        return quantidade > 0 ? Optional.of(maior) : Optional.empty();
    }
}
