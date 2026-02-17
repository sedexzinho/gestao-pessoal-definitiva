# Plano de Migração de Nomenclatura - Padrão de Nomes

## Visão Geral

Este documento detalha todas as alterações necessárias para migrar as tabelas e campos do banco de dados para o novo padrão de nomenclatura definido.

### Regras de Nomenclatura

**Tabelas:**

- Nome em plural
- Idioma: Inglês
- Mnemônico: 6 primeiras letras do nome da tabela

**Campos:**

- Idioma: Inglês
- Padrão: `{mnemônico_tabela}_{tipo}_{nome_campo}`
- Tipos de dados:
  - `id` = BigSerial/Identity
  - `dt` = Data e hora
  - `cd` = Código
  - `vl` = Numérico/valores monetários
  - `ds` = String/Texto
  - `fl` = Booleanos
  - `nr` = Números Inteiros
  - `st` = Status

---

## 1. Tabela: `usuario` → `users`

| Coluna Atual   | Tipo      | Novo Nome               | Novo Tipo |
| -------------- | --------- | ----------------------- | --------- |
| id             | bigint    | users_id                | id        |
| codigo         | bigint    | users_cd                | id        |
| nome           | varchar   | users_ds_name           | ds        |
| salario_mensal | numeric   | users_vl_monthly_salary | vl        |
| criado_em      | timestamp | users_dt_created_at     | dt        |

**Mnemônico:** `users_`

---

## 2. Tabela: `categoria` → `categories`

| Coluna Atual | Tipo    | Novo Nome      | Novo Tipo |
| ------------ | ------- | -------------- | --------- |
| id           | bigint  | catego_id      | id        |
| nome         | varchar | catego_ds_name | ds        |

**Mnemônico:** `catego_`

---

## 3. Tabela: `despesas` → `expenses`

| Coluna Atual       | Tipo    | Novo Nome                            | Novo Tipo | Observação  |
| ------------------ | ------- | ------------------------------------ | --------- | ----------- |
| id                 | bigint  | expens_id                            | id        |             |
| tipo               | varchar | expens_ds_type                       | ds        |             |
| nome               | varchar | expens_ds_name                       | ds        |             |
| valor_pago         | numeric | expens_vl_amount                     | vl        |             |
| status             | varchar | expens_st_status                     | st        |             |
| data_registro      | date    | expens_dt_registered_at              | dt        |             |
| id_categoria       | bigint  | catego_id                            | id        | FK          |
| dia_vencimento     | integer | expens_nr_due_day                    | nr        |             |
| ativa              | boolean | expens_fl_active                     | fl        |             |
| parcelas_restantes | integer | ~~expens_nr_remaining_installments~~ | nr        | **REMOVER** |
| parcela_atual      | integer | expens_nr_current_installment        | nr        |             |
| total_parcelas     | integer | expens_nr_total_installments         | nr        |             |
| valor_parcela      | numeric | expens_vl_installment_amount         | vl        |             |

**Mnemônico:** `expens_`

---

## 4. Tabela: `pagamentos` → ~~`payments`~~

**⚠️ TABELA A SER REMOVIDA**

Esta tabela será completamente removida do banco de dados.

---

## 5. Tabela: `resumo_mensal` → ~~`monthly_summaries`~~

**⚠️ TABELA A SER REMOVIDA**

Esta tabela será completamente removida do banco de dados.

---

## Resumo das Alterações

### Tabelas a Renomear

| Nome Atual    | Novo Nome         | Ação        |
| ------------- | ----------------- | ----------- |
| usuario       | users             | Renomear    |
| categoria     | categories        | Renomear    |
| despesas      | expenses          | Renomear    |
| pagamentos    | payments          | **REMOVER** |
| resumo_mensal | monthly_summaries | **REMOVER** |

### Tabelas Removidas: **2**

### Campos a Remover: **1**

| Tabela   | Campo              |
| -------- | ------------------ |
| despesas | parcelas_restantes |

### Total de Alterações

- **3** tabelas renomeadas
- **25** colunas renomeadas
- **1** foreign key a ser atualizada

---

## Próximos Passos (após aprovação)

1. Remover tabelas `pagamentos` e `resumo_mensal`
2. Remover campo `parcelas_restantes` da tabela `expenses`
3. Renomear tabelas para novos nomes
4. Renomear colunas seguindo o novo padrão
5. Atualizar entidades Java (models)
6. Atualizar repositories
7. Atualizar services
8. Atualizar controllers
9. Atualizar DTOs
10. Testar a aplicação

---

## Script SQL Gerado (Prévia)

```sql
-- ============================================
-- REMOÇÃO DE TABELAS
-- ============================================
DROP TABLE IF EXISTS pagamentos CASCADE;
DROP TABLE IF EXISTS resumo_mensal CASCADE;

-- ============================================
-- RENOMEAR TABELAS
-- ============================================
ALTER TABLE usuario RENAME TO users;
ALTER TABLE categoria RENAME TO categories;
ALTER TABLE despesas RENAME TO expenses;

-- ============================================
-- RENOMEAR COLUNAS - users
-- ============================================
ALTER TABLE users RENAME COLUMN id TO users_id;
ALTER TABLE users RENAME COLUMN codigo TO users_cd;
ALTER TABLE users RENAME COLUMN nome TO users_ds_name;
ALTER TABLE users RENAME COLUMN salario_mensal TO users_vl_monthly_salary;
ALTER TABLE users RENAME COLUMN criado_em TO users_dt_created_at;

-- ============================================
-- RENOMEAR COLUNAS - categories
-- ============================================
ALTER TABLE categories RENAME COLUMN id TO catego_id;
ALTER TABLE categories RENAME COLUMN nome TO catego_ds_name;

-- ============================================
-- RENOMEAR COLUNAS - expenses
-- ============================================
ALTER TABLE expenses RENAME COLUMN id TO expens_id;
ALTER TABLE expenses RENAME COLUMN tipo TO expens_ds_type;
ALTER TABLE expenses RENAME COLUMN nome TO expens_ds_name;
ALTER TABLE expenses RENAME COLUMN valor_pago TO expens_vl_amount;
ALTER TABLE expenses RENAME COLUMN status TO expens_st_status;
ALTER TABLE expenses RENAME COLUMN data_registro TO expens_dt_registered_at;
ALTER TABLE expenses RENAME COLUMN id_categoria TO catego_id;
ALTER TABLE expenses RENAME COLUMN dia_vencimento TO expens_nr_due_day;
ALTER TABLE expenses RENAME COLUMN ativa TO expens_fl_active;
-- REMOVER CAMPO parcelas_restantes
ALTER TABLE expenses DROP COLUMN IF EXISTS parcelas_restantes;
ALTER TABLE expenses RENAME COLUMN parcela_atual TO expens_nr_current_installment;
ALTER TABLE expenses RENAME COLUMN total_parcelas TO expens_nr_total_installments;
ALTER TABLE expenses RENAME COLUMN valor_parcela TO expens_vl_installment_amount;

-- ============================================
-- ATUALIZAR FOREIGN KEYS
-- ============================================
-- A FK em expenses que apontava para id_categoria agora aponta para catego_id
```

---

## Impacto no Código Java

### Entities a serem atualizadas:

- `User.java` - Renomear para `Users.java` e atualizar campos
- `Category.java` - Renomear para `Categories.java` e atualizar campos
- `Expenses.java` - Renomear para `Expenses.java` (manter nome), atualizar campos

### Entities a serem removidas:

- `Pagamentos.java` - Remover completamente
- (criar nova tabela `resumo_mensal` não existe no código Java atual)

### Repositories a serem atualizados:

- `UserRepository.java`
- `CategoryRepository.java`
- `ExpensesRepository.java`

### Repositories a serem removidos:

- `PagamentoRepository.java`

### Services a serem atualizados:

- `UsuarioService.java`
- `ExpensesService.java`

### Services a serem removidos:

- `PagamentoService.java`
- `ScheduledPaymentService.java`

---

**Data de Criação:** 2026-02-17
**Autor:** Plano de Migração Automático
