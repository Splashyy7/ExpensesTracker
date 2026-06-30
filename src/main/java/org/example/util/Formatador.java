package org.example.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilitários de formatação para exibição na interface de console.
 */
public final class Formatador {

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(LOCALE_BR);

    private Formatador() {
    }

    public static String moeda(BigDecimal valor) {
        return MOEDA.format(valor);
    }

    public static String moeda(double valor) {
        return MOEDA.format(valor);
    }
}
