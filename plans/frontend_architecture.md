# Plano de Frontend - Sistema de Gestão Pessoal

## Visão Geral do Projeto

Frontend para sistema de gestão de despesas pessoais usando **Vite + React + JavaScript**.

## Tecnologias

- **Build Tool**: Vite
- **Framework**: React 18+
- **Linguagem**: JavaScript
- **Gerenciamento de Estado**: React Context + Hooks
- **Roteamento**: React Router v6
- **Requisições HTTP**: Axios
- **Formulários**: React Hook Form
- **Validação**: Zod (opcional)
- **Estilização**: Tailwind CSS

## Estrutura de Pastas

```
src/
├── components/          # Componentes reutilizáveis
│   ├── ui/             # Componentes base (Button, Input, Select, Card, Modal)
│   ├── layout/         # Header, Sidebar, Layout
│   └── forms/          # Componentes de formulário
├── pages/              # Páginas da aplicação
│   ├── Login/
│   ├── Dashboard/
│   ├── Expenses/       # Lista e cadastro de despesas
│   ├── Installments/   # Controle de parcelas
│   └── Categories/     # Gestão de categorias
├── services/           # Serviços de API
│   ├── api.ts          # Configuração Axios
│   ├── auth.service.ts
│   ├── expenses.service.ts
│   └── category.service.ts
├── hooks/              # Hooks personalizados
├── context/            # Contextos React
│   └── AuthContext.tsx
├── types/              # Tipos TypeScript
│   ├── expenses.type.ts
│   ├── user.type.ts
│   └── category.type.ts
├── utils/              # Funções utilitárias
├── styles/             # Arquivos de estilo
└── App.tsx             # Componente principal
```

## Páginas e Funcionalidades

### 1. Login

- Formulário de autenticação
- Simulação de login (sem backend real para auth)
- Armazenamento de token no localStorage

### 2. Dashboard

- Resumo de despesas do mês
- Total de despesas fixas
- Total de despesas parceladas
- Total de despesas comuns
- Gráfico simples de despesas por categoria
- Indicadores visuais de saúde financeira

### 3. Cadastro de Despesas

Fluxo de cadastro com lógica condicional:

```
┌─────────────────────────────────────────────────────────────┐
│                    CADASTRO DE DESPESA                      │
├─────────────────────────────────────────────────────────────┤
│  Nome da Despesa: [________________]                        │
│                                                             │
│  Tipo: ◉ COMUM   ◉ PARCELADO   ◉ DESPESA FIXA             │
│                                                             │
│  Categoria: [Selecionar ▼] [+ Nova Categoria]               │
│                                                             │
│  Valor: [________________]                                  │
│                                                             │
│  [Condicional - Se PARCELADO]                               │
│  ├── Total de Parcelas: [______]                            │
│  ├── Parcela Atual: [______]                                │
│  └── Valor por Parcela: [________________]                  │
│                                                             │
│  [Condicional - Se DESPESA FIXA]                            │
│  └── Dia de Vencimento: [______]                            │
│                                                             │
│  [CONCLOIR CADASTRO]                                        │
└─────────────────────────────────────────────────────────────┘
```

#### Lógica de Tipos:

- **COMUM**: Despesa única, valor simples
- **PARCELADO**: Múltiplas parcelas (ex: 12x de R$ 100)
- **DESPESA FIXA**: Recorrente mensal (ex: Netflix, aluguel)

### 4. Lista de Despesas

- Tabela com todas as despesas
- Filtros por:
  - Tipo (COMUM, PARCELADO, FIXO)
  - Categoria
  - Status (ativa/inativa)
  - Data
- Ações: Editar, Excluir, Pagar Parcela

### 5. Controle de Parcelas

- Lista de despesas parceladas
- Visualização de parcelas pagas/pendentes
- Botão para registrar pagamento de parcela
- Progresso visual das parcelas


### 6. Gestão de Categorias

- Lista de categorias existentes
- Criar nova categoria
- Editar categoria
- Excluir categoria (se não estiver em uso)

## Tipos TypeScript

```typescript
// expenses.type.ts
export type ExpenseType = "COMUM" | "PARCELADO" | "FIXO";

export interface Expense {
  id: number;
  name: string;
  type: ExpenseType;
  amount: number;
  status: string;
  registeredAt: string;
  category: Category;
  dueDay?: number;
  active: boolean;
  currentInstallment?: number;
  totalInstallments?: number;
  installmentAmount?: number;
  lastPaymentDate?: string;
  completed: boolean;
}

export interface CreateExpenseDTO {
  nome: string;
  tipo: ExpenseType;
  valorPago: number;
  nomeCategoria: string;
  totalParcelas?: number;
  valorParcela?: number;
  parcelaAtual?: number;
  diaVencimento?: number;
}

// category.type.ts
export interface Category {
  id: number;
  name: string;
}

// user.type.ts
export interface User {
  id: number;
  code: number;
  name: string;
  monthlySalary: number;
}
```

## Integração com Backend

### Endpoints necessários (verificar no backend):

- `POST /api/expenses/add` - Criar despesa
- `POST /api/expenses/pay/{id}` - Pagar parcela
- `GET /api/expenses` - Listar despesas
- `GET /api/categories` - Listar categorias
- `POST /api/categories` - Criar categoria

### Configuração de API

```typescript
// services/api.ts
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

## Variáveis de Ambiente

```
VITE_API_URL=http://localhost:8080
```

## Próximos Passos

1. Criar projeto Vite + React + TypeScript
2. Instalar dependências
3. Configurar estrutura base
4. Implementar páginas uma a uma
5. Testar integração com backend

## Observações

- O backend precisa expor endpoints GET para listar despesas e categorias
- Pode ser necessário criar endpoints adicionais no backend conforme a necessidade
- Considerar paginação para listas grandes
