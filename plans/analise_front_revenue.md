# Análise: O que falta para implementar Revenues no Frontend

## Status Atual

### Backend - ✅ COMPLETO

| Arquivo                                                                                   | Status                           |
| ----------------------------------------------------------------------------------------- | -------------------------------- |
| [`Revenue.java`](src/main/java/com/example/models/Revenue.java)                           | ✅ Implementado                  |
| [`RevenueDTO.java`](src/main/java/com/example/Dto/RevenueDTO.java)                        | ✅ Implementado                  |
| [`RevenuesRepository.java`](src/main/java/com/example/repository/RevenuesRepository.java) | ✅ Implementado com queries JPQL |
| [`RevenueService.java`](src/main/java/com/example/service/RevenueService.java)            | ✅ Implementado                  |
| [`RevenueController.java`](src/main/java/com/example/Controller/RevenueController.java)   | ✅ Implementado                  |

### Frontend - ❌ FALTA IMPLEMENTAR

| Arquivo                                                                  | Status                            | Prioridade |
| ------------------------------------------------------------------------ | --------------------------------- | ---------- |
| [`revenueService.js`](gestao-pessoal/src/services/revenueService.js)     | ❌ Não existe                     | Alta       |
| [`RevenueList.jsx`](gestao-pessoal/src/pages/RevenueList.jsx)            | ❌ Não existe                     | Alta       |
| [`RevenueForm.jsx`](gestao-pessoal/src/pages/RevenueForm.jsx)            | ❌ Não existe                     | Alta       |
| [`App.jsx`](gestao-pessoal/src/App.jsx) - rotas                          | ❌ Falta adicionar rotas          | Alta       |
| [`Sidebar.jsx`](gestao-pessoal/src/components/layout/Sidebar.jsx) - menu | ❌ Falta item Receitas            | Alta       |
| [`Dashboard.jsx`](gestao-pessoal/src/pages/Dashboard.jsx)                | ⏸️ Parcial (verificar integração) | Média      |

---

## Detalhamento das Tarefas

### 1. Criar revenueService.js

Criar o serviço para comunicação com a API:

- `GET /api/revenues` - Listar todas
- `GET /api/revenues/{id}` - Buscar por ID
- `POST /api/revenues` - Criar nova
- `PUT /api/revenues/{id}` - Atualizar
- `DELETE /api/revenues/{id}` - Excluir
- `GET /api/revenues/summary` - Resumo de receitas
- `GET /api/revenues/month/{year}/{month}` - Receitas por mês

### 2. Criar RevenueList.jsx

Página de listagem de receitas:

- Tabela com todas as receitas
- Filtro por mês/ano
- Botões de editar e excluir
- Modal de confirmação para excluir
- Integração com revenueService

### 3. Criar RevenueForm.jsx

Página de formulário para criar/editar:

- Campos: nome, valor, tipo (AVULSO/FIXO), categoria, dia vencimento, data recebimento
- Select de categorias (buscar do categoryService)
- Validações
- Modo edição com dados pré-preenchidos

### 4. Atualizar App.jsx

Adicionar rotas:

```jsx
<Route path="revenues" element={<RevenueList />} />
<Route path="revenues/new" element={<RevenueForm />} />
<Route path="revenues/:id/edit" element={<RevenueForm />} />
```

### 5. Atualizar Sidebar.jsx

Adicionar item de menu "Receitas" com ícone apropriado.

### 6. Atualizar Dashboard.jsx (Opcional)

Integrar dados de receitas nos cards do dashboard:

- Total de receitas
- Receitas Fixas
- Receitas Avulsas
- Receitas Pendentes

---

## API Endpoints Disponíveis

| Método | Endpoint                             | Descrição               |
| ------ | ------------------------------------ | ----------------------- |
| GET    | `/api/revenues`                      | Lista todas as receitas |
| GET    | `/api/revenues/{id}`                 | Busca receita por ID    |
| POST   | `/api/revenues`                      | Cria nova receita       |
| PUT    | `/api/revenues/{id}`                 | Atualiza receita        |
| DELETE | `/api/revenues/{id}`                 | Exclui receita          |
| GET    | `/api/revenues/summary`              | Retorna totais          |
| GET    | `/api/revenues/month/{year}/{month}` | Receitas por mês        |

---

## RevenueDTO - Estrutura esperada

```javascript
{
  nome: "Salário",
  tipo: "FIXO" | "AVULSO",
  valor: 5000.00,
  nomeCategoria: "Salário",
  diaVencimento: 5,  // opcional, para FIXO
  ativa: true,       // opcional
  dataRecebimento: "2026-02-01"  // opcional
}
```

---

## Próximos Passos

Para prosseguir com a implementação, precisamos:

1. **Confirmar**: Os nomes dos arquivos que deseja criar
2. **Prioridade**: Se deseja começar por algum componente específico
3. **Estilo**: Se quer seguir o mesmo padrão de ExpensesList/ExpenseForm

O fluxo recomendado é:

1. revenueService.js (base)
2. RevenueList.jsx (listagem)
3. RevenueForm.jsx (formulário)
4. App.jsx + Sidebar.jsx (integração)
5. Dashboard.jsx (opcional)
