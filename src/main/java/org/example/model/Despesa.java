package org.example.model;

import org.example.exception.ValidacaoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa uma despesa pessoal com identificador único, valor monetário e metadados.
 */
public class Despesa {

    private static final int DESCRICAO_MAX = 200;
    private static final int CATEGORIA_MAX = 50;

    private final long id;
    private String descricao;
    private BigDecimal valor;
    private LocalDate data;
    private String categoria;

    public Despesa(long id, String descricao, BigDecimal valor, String categoria, LocalDate data) {
        this.id = id;
        validarDescricao(descricao);
        validarValor(valor);
        validarCategoria(categoria);
        validarData(data);

        this.descricao = descricao.trim();
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.categoria = categoria.trim();
        this.data = data;
    }

    public Despesa(String descricao, BigDecimal valor, String categoria) {
        this(0, descricao, valor, categoria, LocalDate.now());
    }

    public Despesa(String descricao, BigDecimal valor, String categoria, LocalDate data) {
        this(0, descricao, valor, categoria, data);
    }

    public long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDate getData() {
        return data;
    }

    public void atualizar(String descricao, BigDecimal valor, String categoria, LocalDate data) {
        validarDescricao(descricao);
        validarValor(valor);
        validarCategoria(categoria);
        validarData(data);

        this.descricao = descricao.trim();
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.categoria = categoria.trim();
        this.data = data;
    }

    public Despesa comId(long novoId) {
        return new Despesa(novoId, descricao, valor, categoria, data);
    }

    private static void validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new ValidacaoException("A descrição não pode ser vazia.");
        }
        if (descricao.trim().length() > DESCRICAO_MAX) {
            throw new ValidacaoException("A descrição deve ter no máximo " + DESCRICAO_MAX + " caracteres.");
        }
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new ValidacaoException("O valor não pode ser nulo.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O valor deve ser maior que zero.");
        }
        if (valor.compareTo(new BigDecimal("999999999.99")) > 0) {
            throw new ValidacaoException("O valor excede o limite permitido.");
        }
    }

    private static void validarCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new ValidacaoException("A categoria não pode ser vazia.");
        }
        if (categoria.trim().length() > CATEGORIA_MAX) {
            throw new ValidacaoException("A categoria deve ter no máximo " + CATEGORIA_MAX + " caracteres.");
        }
    }

    private static void validarData(LocalDate data) {
        if (data == null) {
            throw new ValidacaoException("A data não pode ser nula.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new ValidacaoException("A data não pode ser no futuro.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Despesa despesa)) {
            return false;
        }
        return id == despesa.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("#%d | %s | %s | R$ %s (%s)",
                id, data, descricao, valor.toPlainString(), categoria);
    }
}
