package org.example.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configurações de orçamento persistidas entre sessões.
 */
public class ConfiguracaoFinanceira {

    private BigDecimal orcamentoMensalPadrao;
    private final Map<String, BigDecimal> limitesPorCategoria;

    public ConfiguracaoFinanceira() {
        this.orcamentoMensalPadrao = null;
        this.limitesPorCategoria = new HashMap<>();
    }

    public ConfiguracaoFinanceira(BigDecimal orcamentoMensalPadrao, Map<String, BigDecimal> limitesPorCategoria) {
        this.orcamentoMensalPadrao = orcamentoMensalPadrao;
        this.limitesPorCategoria = new HashMap<>(limitesPorCategoria != null ? limitesPorCategoria : Map.of());
    }

    public Optional<BigDecimal> getOrcamentoMensalPadrao() {
        return Optional.ofNullable(orcamentoMensalPadrao);
    }

    public void setOrcamentoMensalPadrao(BigDecimal limite) {
        this.orcamentoMensalPadrao = limite;
    }

    public Map<String, BigDecimal> getLimitesPorCategoria() {
        return Collections.unmodifiableMap(limitesPorCategoria);
    }

    public void definirLimiteCategoria(String categoria, BigDecimal limite) {
        limitesPorCategoria.put(categoria.trim(), limite);
    }

    public Optional<BigDecimal> getLimiteCategoria(String categoria) {
        if (categoria == null) {
            return Optional.empty();
        }
        return limitesPorCategoria.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(categoria.trim()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public void removerLimiteCategoria(String categoria) {
        limitesPorCategoria.entrySet().removeIf(e -> e.getKey().equalsIgnoreCase(categoria.trim()));
    }
}
