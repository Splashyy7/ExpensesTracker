package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class GerenciadorDespesas {
    private List<Despesa> despesas;

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    public GerenciadorDespesas() {
        this.despesas = new ArrayList<>();
    }

    public void adicionarDespesa(String descricao, double valor, String categoria) {
        Despesa novaDespesa = new Despesa(descricao, valor, categoria);
        despesas.add(novaDespesa);
        System.out.println("Despesa adicionada com sucesso!");
    }

    public void listarDespesas() {
        if (despesas.isEmpty()) {
            System.out.println("Nenhuma despesa cadastrada.");
        } else {
            System.out.println("\n--- Lista de Despesas ---");
            for (Despesa d : despesas) {
                System.out.println(d);
            }
        }
    }

    public void calcularTotal() {
        double total = despesas.stream()
                .mapToDouble(Despesa::getValor)
                .sum();

        System.out.printf("\nTotal Gasto: R$ %.2f\n", total);
    }
}