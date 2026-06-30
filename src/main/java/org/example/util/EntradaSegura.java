package org.example.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Encapsula leitura de entrada do usuário com validação e recuperação de erros.
 */
public class EntradaSegura {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner scanner;

    public EntradaSegura(Scanner scanner) {
        this.scanner = scanner;
    }

    public int lerInteiro(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int valor = scanner.nextInt();
                scanner.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
    }

    public BigDecimal lerValorMonetario(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = scanner.nextLine().trim().replace(',', '.');
            if (linha.isEmpty()) {
                System.out.println("O valor não pode ser vazio.");
                continue;
            }
            try {
                return new BigDecimal(linha);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Use formato numérico (ex: 25.50).");
            }
        }
    }

    public String lerTexto(String prompt, boolean obrigatorio) {
        while (true) {
            System.out.print(prompt);
            String texto = scanner.nextLine();
            if (!obrigatorio || !texto.trim().isEmpty()) {
                return texto;
            }
            System.out.println("Este campo é obrigatório.");
        }
    }

    public LocalDate lerData(String prompt, boolean permitirVazioParaHoje) {
        while (true) {
            System.out.print(prompt);
            String linha = scanner.nextLine().trim();
            if (linha.isEmpty() && permitirVazioParaHoje) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(linha, FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }

    public long lerId(String prompt) {
        while (true) {
            long id = lerInteiro(prompt);
            if (id <= 0) {
                System.out.println("O ID deve ser um número positivo.");
                continue;
            }
            return id;
        }
    }

    public void pausar() {
        System.out.print("\nPressione Enter para continuar...");
        scanner.nextLine();
    }
}
