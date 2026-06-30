# 💰 Expenses Tracker (Gerenciador de Despesas)

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

Aplicação de console (CLI) em Java para gerenciamento de despesas pessoais. O projeto demonstra arquitetura em camadas, validação de entrada, persistência em JSON, exportação CSV e testes unitários.

## Funcionalidades

| Recurso | Descrição |
|---------|-----------|
| CRUD completo | Adicionar, listar, editar e remover despesas |
| Persistência | Dados salvos automaticamente em JSON |
| Filtros | Por categoria, período e busca por descrição |
| Relatórios | Total geral e totais agrupados por categoria |
| Orçamento | Verificação de limite mensal com alerta |
| Exportação | Geração de arquivo CSV para planilhas |
| Validação | Valores positivos, datas sem futuro, limites de tamanho |
| Entrada segura | Tratamento de entradas inválidas sem crash |

## Arquitetura

```
src/main/java/org/example/
├── Main.java                    # Interface CLI (apresentação)
├── model/Despesa.java           # Entidade de domínio
├── service/GerenciadorDespesas  # Lógica de negócio
├── persistence/                 # Repositório JSON
├── exportacao/ExportadorCsv     # Exportação CSV
├── exception/ValidacaoException # Erros de validação
└── util/                        # Entrada segura e formatação
```

## Tecnologias

- **Java 17+** — linguagem principal
- **Maven** — build e dependências
- **JUnit 5** — testes unitários
- **Gson** — serialização JSON

## Pré-requisitos

- [JDK 17](https://adoptium.net/) ou superior
- [Apache Maven 3.8+](https://maven.apache.org/)

## Como executar

```bash
# Clonar o repositório
git clone https://github.com/Splashyy7/ExpensesTracker.git
cd ExpensesTracker

# Compilar
mvn compile

# Executar
mvn exec:java

# Ou gerar JAR e executar
mvn package -DskipTests
java -jar target/ExpensesTracker-1.0-SNAPSHOT.jar
```

## Testes

```bash
# Rodar todos os testes
mvn test
```

## Configuração

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `EXPENSES_DATA_FILE` | Caminho do arquivo JSON de dados | `~/.expenses-tracker/despesas.json` |

Exemplo:

```bash
export EXPENSES_DATA_FILE=/caminho/seguro/minhas_despesas.json
mvn exec:java
```

## Menu da aplicação

```
 1. Adicionar despesa
 2. Listar despesas (com ordenação)
 3. Ver total gasto
 4. Totais por categoria
 5. Filtrar por categoria
 6. Filtrar por período
 7. Buscar por descrição
 8. Editar despesa
 9. Remover despesa
10. Verificar orçamento mensal
11. Exportar para CSV
 0. Sair
```

## Segurança e boas práticas

- Valores monetários com `BigDecimal` (sem erros de ponto flutuante)
- Validação de entrada no domínio e na CLI
- Persistência com escrita atômica (arquivo temporário + rename)
- Caminhos normalizados para evitar path traversal
- Dados locais sem exposição em rede
- Lista interna protegida com `Collections.unmodifiableList`

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.
