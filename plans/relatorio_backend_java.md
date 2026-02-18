# Relatório de Implementação - Backend Java

## Visão Geral

Este documento detalha todas as alterações e implementações realizadas no backend Java (Spring Boot) para suportar o frontend React.

---

## 1. AuthController.java

**Arquivo:** `src/main/java/com/example/Controller/AuthController.java`

**Descrição:** Controller para autenticação de usuários.

### Endpoints Criados

| Método | Endpoint             | Descrição                |
| ------ | -------------------- | ------------------------ |
| POST   | `/api/auth/login`    | Login de usuário         |
| POST   | `/api/auth/register` | Registro de novo usuário |

### Detalhes

- **POST /api/auth/login**
  - Recebe email e senha no corpo da requisição
  - Valida se a senha tem pelo menos 6 caracteres
  - Busca usuário no banco ou cria um mock automaticamente
  - Retorna: id, email, name, token

- **POST /api/auth/register**
  - Recebe name e email no corpo da requisição
  - Cria novo usuário no banco de dados
  - Retorna: id, email, name, token

---

## 2. UserController.java

**Arquivo:** `src/main/java/com/example/Controller/UserController.java`

**Descrição:** Controller para gerenciamento do perfil do usuário.

### Endpoints Criados

| Método | Endpoint        | Descrição                    |
| ------ | --------------- | ---------------------------- |
| GET    | `/api/users/me` | Obter dados do usuário atual |
| PUT    | `/api/users/me` | Atualizar perfil do usuário  |

### Detalhes

- **GET /api/users/me**
  - Retorna os dados do usuário logado (ID fixo 1)
  - Resposta: id, name, email

- **PUT /api/users/me**
  - Atualiza o nome do usuário
  - Retorna os dados atualizados

---

## 3. CategoriesController.java

**Arquivo:** `src/main/java/com/example/Controller/CategoriesController.java`

**Descrição:** Controller para gerenciamento de categorias de despesas.

### Endpoints Criados/Atualizados

| Método | Endpoint               | Descrição                  |
| ------ | ---------------------- | -------------------------- |
| GET    | `/api/categories`      | Listar todas as categorias |
| GET    | `/api/categories/{id}` | Buscar categoria por ID    |
| POST   | `/api/categories`      | Criar nova categoria       |
| PUT    | `/api/categories/{id}` | Atualizar categoria        |
| DELETE | `/api/categories/{id}` | Excluir categoria          |

### Detalhes

- Aceita payload com campos: name, color, icon (color e icon são ignorados no momento)
- Retorna objeto Category com: id, name

---

## 4. ExpensesController.java

**Arquivo:** `src/main/java/com/example/Controller/ExpensesController.java`

**Descrição:** Controller principal para gerenciamento de despesas.

### Endpoints Criados/Atualizados

| Método | Endpoint                             | Descrição                |
| ------ | ------------------------------------ | ------------------------ |
| GET    | `/api/expenses`                      | Listar todas as despesas |
| GET    | `/api/expenses/{id}`                 | Buscar despesa por ID    |
| POST   | `/api/expenses`                      | Criar nova despesa       |
| PUT    | `/api/expenses/{id}`                 | Atualizar despesa        |
| DELETE | `/api/expenses/{id}`                 | Excluir despesa          |
| GET    | `/api/expenses/summary`              | Obter resumo de despesas |
| GET    | `/api/expenses/month/{year}/{month}` | Listar despesas por mês  |

### Detalhes

- **POST /api/expenses**
  - Recebe ExpensesDTO com campos: nome, tipo, valorPago, nomeCategoria, totalParcelas, parcelaAtual, diaVencimento
  - Cria automaticamente categoria se não existir
  - Calcula valor da parcela automaticamente
  - Define status inicial: PAGO para AVULSO, PENDENTE para PARCELADO/FIXO

- **PUT /api/expenses/{id}**
  - Atualiza: name, amount, type, dueDay, totalInstallments, currentInstallment
  - Retorna despesa atualizada

- **GET /api/expenses/summary**
  - Retorna: totalExpenses, totalInstallments, pendingInstallments

---

## 5. InstallmentsController.java

**Arquivo:** `src/main/java/com/example/Controller/InstallmentsController.java`

**Descrição:** Controller para gerenciamento de parcelas de despesas.

### Endpoints Criados

| Método | Endpoint                                 | Descrição                      |
| ------ | ---------------------------------------- | ------------------------------ |
| GET    | `/api/installments`                      | Listar todas as parcelas       |
| GET    | `/api/installments/pending`              | Listar parcelas pendentes      |
| GET    | `/api/installments/expense/{expenseId}`  | Listar parcelas de uma despesa |
| GET    | `/api/installments/month/{year}/{month}` | Listar parcelas de um mês      |
| POST   | `/api/installments/{id}/pay`             | Marcar parcela como paga       |
| GET    | `/api/installments/summary`              | Obter resumo de parcelas       |

### Detalhes

- **Estrutura do ID de parcela:** O ID é composto por `expenseId * 100 + installmentNumber`
  - Exemplo: despesa ID 5, parcela 3 → ID = 503

- **GET /api/installments**
  - Retorna todas as parcelas de despesas PARCELADO e FIXO
  - Campos retornados: id, expenseId, description, amount, installmentNumber, totalInstallments, paid, dueDate

- **POST /api/installments/{id}/pay**
  - Marca parcela como paga
  - Chama InstallmentSchedulerService.processPaymentManual()

- **GET /api/installments/summary**
  - Retorna: totalPending, totalPaid, pendingCount

---

## 6. Models (Entidades)

### Expenses.java

**Campos do banco de dados:**

| Campo              | Tipo              | Descrição                     |
| ------------------ | ----------------- | ----------------------------- |
| id                 | Long              | ID único                      |
| type               | String            | Tipo: AVULSO, PARCELADO, FIXO |
| name               | String            | Nome/descrição                |
| amount             | BigDecimal        | Valor total                   |
| status             | String            | Status: PAGO, PENDENTE        |
| registeredAt       | LocalDate         | Data de registro              |
| category           | Category          | Categoria关联                 |
| dueDay             | Integer           | Dia de vencimento             |
| active             | Boolean           | Se está ativa                 |
| currentInstallment | Integer           | Parcela atual                 |
| totalInstallments  | Total de parcelas |
| installmentAmount  | BigDecimal        | Valor da parcela              |
| lastPaymentDate    | LocalDate         | Data do último pagamento      |
| completed          | Boolean           | Se está concluída             |

### Category.java

**Campos do banco de dados:**

| Campo | Tipo   | Descrição         |
| ----- | ------ | ----------------- |
| id    | Long   | ID único          |
| name  | String | Nome da categoria |

### User.java

**Campos do banco de dados:**

| Campo         | Tipo       | Descrição       |
| ------------- | ---------- | --------------- |
| id            | Long       | ID único        |
| name          | String     | Nome do usuário |
| code          | Long       | Código interno  |
| monthlySalary | BigDecimal | Salário mensal  |

---

## 7. DTOs

### ExpensesDTO.java

**Campos esperados do frontend:**

```java
record ExpensesDTO(
    String nome,           // Nome da despesa
    String tipo,           // AVULSO, PARCELADO, FIXO
    BigDecimal valorPago,  // Valor pago
    String nomeCategoria,  // Nome da categoria
    Integer totalParcelas, // Total de parcelas
    BigDecimal valorParcela,
    Integer parcelaAtual,
    Integer parcelasRestantes,
    Boolean ativa,
    Integer diaVencimento
)
```

---

## 8. Services

### ExpensesService.java

**Métodos principais:**

| Método                      | Descrição             |
| --------------------------- | --------------------- |
| registrarGasto(ExpensesDTO) | Registra nova despesa |
| consultarSaldoSimples()     | Calcula saldo atual   |

**Lógica de registro:**

1. Busca ou cria categoria pelo nome
2. Mapeia DTO para entidade Expenses
3. Calcula valor da parcela = valorPago / totalParcelas
4. Define status inicial baseado no tipo
5. Para AVULSO: desconta do saldo automaticamente

### InstallmentSchedulerService.java

**Métodos principais:**

| Método                               | Descrição                            |
| ------------------------------------ | ------------------------------------ |
| processPaymentManual(Long expenseId) | Processa pagamento manual de parcela |

---

## 9. Repositories

### ExpensesRepository.java

- Herda de JpaRepository<Expenses, Long>
- Métodos padrão: findAll(), findById(), save(), deleteById()

### CategoryRepository.java

- Herda de JpaRepository<Category, Long>
- Métodos padrão + findByNameIgnoreCase()

### UserRepository.java

- Herda de JpaRepository<User, Long>

---

## 10. Resumo de Endpoints da API

| Endpoint                                 | Método | Descrição            |
| ---------------------------------------- | ------ | -------------------- |
| `/api/auth/login`                        | POST   | Login                |
| `/api/auth/register`                     | POST   | Registro             |
| `/api/users/me`                          | GET    | Dados usuário        |
| `/api/users/me`                          | PUT    | Atualizar usuário    |
| `/api/categories`                        | GET    | Listar categorias    |
| `/api/categories/{id}`                   | GET    | Buscar categoria     |
| `/api/categories`                        | POST   | Criar categoria      |
| `/api/categories/{id}`                   | PUT    | Atualizar categoria  |
| `/api/categories/{id}`                   | DELETE | Excluir categoria    |
| `/api/expenses`                          | GET    | Listar despesas      |
| `/api/expenses/{id}`                     | GET    | Buscar despesa       |
| `/api/expenses`                          | POST   | Criar despesa        |
| `/api/expenses/{id}`                     | PUT    | Atualizar despesa    |
| `/api/expenses/{id}`                     | DELETE | Excluir despesa      |
| `/api/expenses/summary`                  | GET    | Resumo despesas      |
| `/api/expenses/month/{year}/{month}`     | GET    | Despesas por mês     |
| `/api/installments`                      | GET    | Listar parcelas      |
| `/api/installments/pending`              | GET    | Parcelas pendentes   |
| `/api/installments/expense/{id}`         | GET    | Parcelas por despesa |
| `/api/installments/month/{year}/{month}` | GET    | Parcelas por mês     |
| `/api/installments/{id}/pay`             | POST   | Pagar parcela        |
| `/api/installments/summary`              | GET    | Resumo parcelas      |

---

## 11. Configurações

### application.properties

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

---

## 12. CORS

Todos os controllers possuem `@CrossOrigin("*")` para permitir requisições do frontend React em qualquer domínio/porta.

---

## Conclusão

O backend Java foi implementado com toda a estrutura necessária para suportar o frontend React:

- Autenticação e gerenciamento de usuários
- CRUD completo de categorias
- CRUD completo de despesas
- Sistema de parcelas automático
- Endpoints de resumo e análise

Os endpoints estão RESTful e seguem as convenções do Spring Boot com JPA/Hibernate.
