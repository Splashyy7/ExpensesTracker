package org.example;

import java.time.LocalDate;

public class Despesa {
    private String descricao;
    private double valor;
    private LocalDate data;
    private String categoria;

    public Despesa(String descricao, double valor, String categoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.categoria = categoria;
        this.data = LocalDate.now();
    }

    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public String getCategoria() { return categoria; }
    public LocalDate getData() { return data; }

    @Override
    public String toString() {
        return data + " | " + descricao + " | R$ " + String.format("%.2f", valor) + " (" + categoria + ")";
    }
}