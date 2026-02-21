# Plano de Migração de Nomenclatura - Tabela Revenue

## Visão Geral

Este documento detalha as alterações necessárias para migrar a tabela `revenues` para o novo padrão de nomenclatura definido no [`renaming_plan.md`](renaming_plan.md).

### Regras de Nomenclatura (do plano original)

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

## 1. Tabela: `revenues`

| Coluna Atual  | Tipo    | Novo Nome              | Novo Tipo | Observação |
| ------------- | ------- | ---------------------- | --------- | ---------- |
| id            | bigint  | reven_id               | id        |            |
| name          | varchar | reven_ds_name          | ds        |            |
| amount        | numeric | reven_vl_amount        | vl        |            |
| status        | varchar | reven_st_status        | st        |            |
| type          | varchar | reven_ds_type          | ds        |            |
| registered_at | date    | reven_dt_registered_at | dt        |            |
| received_date | date    | reven_dt_received_at   | dt        |            |
| id_categoria  | bigint  | catego_id              | id        | FK         |
| active        | boolean | reven_fl_active        | fl        |            |
| due_day       | integer | reven_nr_due_day       | nr        |            |

**Mnemônico:** `reven_`

---

## Resumo das Alterações

### Colunas a Renomear: 10

| Nome Atual    | Novo Nome              |
| ------------- | ---------------------- |
| id            | reven_id               |
| name          | reven_ds_name          |
| amount        | reven_vl_amount        |
| status        | reven_st_status        |
| type          | reven_ds_type          |
| registered_at | reven_dt_registered_at |
| received_date | reven_dt_received_at   |
| id_categoria  | catego_id              |
| active        | reven_fl_active        |
| due_day       | reven_nr_due_day       |

---

## Script SQL Gerado

```sql
-- ============================================
-- RENOMEAR COLUNAS - revenues
-- ============================================
ALTER TABLE revenues RENAME COLUMN id TO reven_id;
ALTER TABLE revenues RENAME COLUMN name TO reven_ds_name;
ALTER TABLE revenues RENAME COLUMN amount TO reven_vl_amount;
ALTER TABLE revenues RENAME COLUMN status TO reven_st_status;
ALTER TABLE revenues RENAME COLUMN type TO reven_ds_type;
ALTER TABLE revenues RENAME COLUMN registered_at TO reven_dt_registered_at;
ALTER TABLE revenues RENAME COLUMN received_date TO reven_dt_received_at;
ALTER TABLE revenues RENAME COLUMN id_categoria TO catego_id;
ALTER TABLE revenues RENAME COLUMN active TO reven_fl_active;
ALTER TABLE revenues RENAME COLUMN due_day TO reven_nr_due_day;

-- ============================================
-- ATUALIZAR FOREIGN KEYS
-- ============================================
-- A FK em revenues que apontava para id_categoria agora aponta para catego_id
```

---

## Impacto no Código Java

### Entities a serem atualizadas:

- [`Revenue.java`](src/main/java/com/example/models/Revenue.java) - Atualizar anotações @Column

### Repositories a serem atualizados:

- [`RevenuesRepository.java`](src/main/java/com/example/repository/RevenuesRepository.java) - Verificar referências

### Services a serem atualizados:

- [`RevenueService.java`](src/main/java/com/example/service/RevenueService.java) - Verificar referências

### Controllers a serem atualizados:

- [`RevenueController.java`](src/main/java/com/example/Controller/RevenueController.java) - Verificar referências

### DTOs a serem atualizados:

- [`RevenueDTO.java`](src/main/java/com/example/Dto/RevenueDTO.java) - Verificar referências

---

## Próximos Passos (após aprovação)

1. Executar script SQL de renomeação das colunas
2. Atualizar entidade Revenue.java com novas anotações @Column
3. Atualizar RevenuesRepository.java se necessário
4. Atualizar RevenueService.java se necessário
5. Atualizar RevenueController.java se necessário
6. Atualizar RevenueDTO.java se necessário
7. Testar a aplicação

---

**Data de Criação:** 2026-02-20
**Autor:** Plano de Migração - Revenue
