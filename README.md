# 💰 Expenses Tracker (Gerenciador de Despesas)

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Aplicação de console (CLI) em Java para gerenciamento de despesas pessoais. Demonstra arquitetura em camadas, validação de entrada, persistência JSON, exportação/importação CSV, orçamentos configuráveis e execução via Docker.

## Funcionalidades

| Recurso | Descrição |
|---------|-----------|
| CRUD completo | Adicionar, listar, editar e remover despesas |
| Persistência | Dados e configurações salvos automaticamente em JSON |
| Filtros | Por categoria, período e busca por descrição |
| Relatórios | Total geral, totais por categoria e resumo mensal |
| Estatísticas | Média, menor e maior despesa (geral ou por período) |
| Orçamento | Limite mensal e limites por categoria com alertas |
| Exportação / Importação | CSV compatível com planilhas |
| Backup | Cópia de segurança automática com timestamp |
| Docker | Container pronto para uso com volume persistente |
| Validação | Valores positivos, datas sem futuro, entrada segura |

## Arquitetura

```
src/main/java/org/example/
├── Main.java                       # Interface CLI
├── model/
│   ├── Despesa.java                # Entidade de domínio
│   ├── ConfiguracaoFinanceira.java # Orçamentos persistidos
│   ├── EstatisticasDespesas.java   # Métricas agregadas
│   └── ResumoMensal.java           # Relatório mensal
├── service/
│   ├── GerenciadorDespesas.java    # Lógica de negócio
│   └── ServicoBackup.java          # Backup de dados
├── persistence/                    # Repositório JSON atômico
├── exportacao/                     # Importador e exportador CSV
├── exception/                      # Exceções de validação
└── util/                           # Entrada segura e formatação
```

## Tecnologias

- **Java 17+** — linguagem principal
- **Maven** — build, testes e fat JAR
- **JUnit 5** — testes unitários
- **Gson** — serialização JSON
- **Docker** — containerização

## Pré-requisitos

Escolha uma das opções:

| Método | Requisitos |
|--------|------------|
| Local | [JDK 17+](https://adoptium.net/) e [Maven 3.8+](https://maven.apache.org/) |
| Docker | [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/) |

## Execução local

```bash
git clone https://github.com/Splashyy7/ExpensesTracker.git
cd ExpensesTracker

# Compilar e testar
mvn test

# Executar via Maven
mvn exec:java

# Ou gerar fat JAR (inclui dependências) e executar
mvn package -DskipTests
java -jar target/ExpensesTracker-1.0-SNAPSHOT.jar

# Ajuda
java -jar target/ExpensesTracker-1.0-SNAPSHOT.jar --help
```

## Execução com Docker

### Build e execução interativa

```bash
# Build da imagem
docker compose build

# Executar (modo interativo — necessário para CLI)
docker compose run --rm expenses-tracker
```

### Apenas Docker (sem Compose)

```bash
docker build -t expenses-tracker .
docker run -it --rm \
  -v expenses-data:/data \
  -e EXPENSES_DATA_FILE=/data/despesas.json \
  expenses-tracker
```

### Persistência de dados

Os dados ficam no volume Docker `expenses-data`, mapeado para `/data` no container:

```
/data/
├── despesas.json      # despesas + configuração de orçamento
└── backups/           # backups criados pela opção 16 do menu
```

Para usar um caminho local em vez do volume nomeado:

```bash
docker run -it --rm \
  -v "$(pwd)/dados:/data" \
  expenses-tracker
```

### Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste se necessário:

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `EXPENSES_DATA_FILE` | Caminho do arquivo JSON | `~/.expenses-tracker/despesas.json` (local) ou `/data/despesas.json` (Docker) |

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
12. Importar de CSV
13. Resumo mensal
14. Estatísticas gerais
15. Configurar orçamento
16. Criar backup dos dados
 0. Sair
```

## Formato dos dados

Arquivo JSON (`despesas.json`):

```json
{
  "proximoId": 3,
  "despesas": [
    {
      "id": 1,
      "descricao": "Uber",
      "valor": "25.50",
      "categoria": "Transporte",
      "data": "2025-06-01"
    }
  ],
  "configuracao": {
    "orcamentoMensalPadrao": "2000.00",
    "limitesPorCategoria": {
      "Alimentação": "500.00"
    }
  }
}
```

Arquivos JSON antigos (sem `configuracao`) continuam compatíveis.

## Testes

```bash
mvn test
```

Cobertura atual: modelo, serviço, persistência, importação/exportação CSV, backup e configuração financeira.

## Segurança e boas práticas

- Valores monetários com `BigDecimal`
- Validação no domínio e na CLI (sem crash em entrada inválida)
- Persistência com escrita atômica (`.tmp` + rename)
- Caminhos normalizados contra path traversal
- Container roda como usuário não-root (`appuser`)
- Dados locais, sem exposição em rede

## Licença

MIT — veja [LICENSE](LICENSE).
