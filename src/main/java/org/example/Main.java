package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GerenciadorDespesas gerenciador = new GerenciadorDespesas();

        while (true) {
            System.out.println("\n--- CONTROLE DE DESPESAS ---");
            System.out.println("1. Adicionar Despesa");
            System.out.println("2. Listar Despesas");
            System.out.println("3. Ver Total Gasto");
            System.out.println("4. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    System.out.print("Descrição: ");
                    String desc = scanner.nextLine();

                    System.out.print("Valor: ");
                    double valor = scanner.nextDouble();
                    scanner.nextLine();

                    System.out.print("Categoria (Ex: Comida, Transporte): ");
                    String cat = scanner.nextLine();

                    gerenciador.adicionarDespesa(desc, valor, cat);
                    break;
                case 2:
                    gerenciador.listarDespesas();
                    break;
                case 3:
                    gerenciador.calcularTotal();
                    break;
                case 4:
                    System.out.println("Saindo...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }
}