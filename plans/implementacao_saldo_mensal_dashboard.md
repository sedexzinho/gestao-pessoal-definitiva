# Plano de Implementação: Saldo Mensal no Dashboard

## Objetivo

Adicionar no Dashboard:

1. **Total de Receitas do Mês** - soma de todas as receitas (FIXO + AVULSO) do mês atual
2. **Total de Despesas do Mês** - soma de todas as despesas (PARCELADO + FIXO + AVULSO) do mês atual
3. **Saldo do Mês** - resultado: Receitas - Despesas

---

## Backend - Implementação

### 1. Adicionar queries no [`RevenuesRepository.java`](src/main/java/com/example/repository/RevenuesRepository.java)

Adicionar dois novos métodos para somar receitas do mês atual:

```java
// Soma todas as receitas do mês atual (independentemente do status)
@Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE YEAR(r.registeredAt) = :year AND MONTH(r.registeredAt) = :month")
BigDecimal sumByMonth(int year, int month);

// Soma receitas FIXAS e AVULSAS do mês atual (receitas esperadas)
@Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE YEAR(r.registeredAt) = :year AND MONTH(r.registeredAt) = :month AND r.type IN ('FIXO', 'AVULSO')")
BigDecimal sumExpectedByMonth(int year, int month);
```

### 2. Adicionar queries no [`ExpensesRepository.java`](src/main/java/com/example/repository/ExpensesRepository.java)

Adicionar método para somar despesas do mês atual:

```java
// Soma todas as despesas do mês atual
@Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expenses e WHERE YEAR(e.registeredAt) = :year AND MONTH(e.registeredAt) = :month")
BigDecimal sumByMonth(int year, int month);

// Soma despesas por tipo do mês atual
@Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expenses e WHERE YEAR(e.registeredAt) = :year AND MONTH(e.registeredAt) = :month AND e.type = :type")
BigDecimal sumByMonthAndType(int year, int month, String type);
```

### 3. Criar novo endpoint

Criar um controller dedicado para o dashboard ou adicionar no [`RevenueController.java`](src/main/java/com/example/Controller/RevenueController.java) ou [`ExpensesController.java`](src/main/java/com/example/Controller/ExpensesController.java).

**Recomendação**: Criar um [`DashboardController.java`](src/main/java/com/example/Controller/DashboardController.java) novo:

```java
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    @Autowired
    private RevenuesRepository revenueRepository;

    @Autowired
    private ExpensesRepository expensesRepository;

    @GetMapping("/monthly-summary")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySummary() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        BigDecimal totalRevenue = revenueRepository.sumByMonth(year, month);
        BigDecimal totalExpenses = expensesRepository.sumByMonth(year, month);
        BigDecimal balance = totalRevenue.subtract(totalExpenses);

        return ResponseEntity.ok(Map.of(
            "totalRevenue", totalRevenue,
            "totalExpenses", totalExpenses,
            "balance", balance
        ));
    }
}
```

---

## Frontend - Implementação

### 1. Adicionar método no [`expenseService.js`](gestao-pessoal/src/services/expenseService.js)

```javascript
getMonthlySummary: async () => {
  const response = await api.get("/dashboard/monthly-summary");
  return response.data;
},
```

### 2. Atualizar o [`Dashboard.jsx`](gestao-pessoal/src/pages/Dashboard.jsx)

Adicionar novos campos no state e consumir o novo endpoint:

```jsx
const [summary, setSummary] = useState({
  totalRevenue: 0,
  totalExpenses: 0,
  balance: 0,
  // ... outros campos existentes
});

// No useEffect:
const monthlySummary = await expenseService.getMonthlySummary();
setSummary((prev) => ({
  ...prev,
  totalRevenue: monthlySummary.totalRevenue || 0,
  totalExpenses: monthlySummary.totalExpenses || 0,
  balance: monthlySummary.balance || 0,
}));
```

### 3. Adicionar novos Cards no Dashboard

Adicionar cards para:

- Total de Receitas do Mês
- Saldo do Mês (destacando positivo/negativo)

---

## Considerações Importantes

### Sobre o campo `registeredAt`:

- **Receitas**: usar `registeredAt` para filtrar por mês
- **Despesas**: usar `registeredAt` para filtrar por mês

### Alternativa para considerar despesas fixas mensais:

Se quiser considerar despesas FIXAS que Repeat mensalmente (não apenas as registradas no mês), seria necessário usar o campo `dueDay` junto com lógica adicional. Por exemplo, todas as despesas FIXAS ativas devem ser consideradas como despesas do mês atual, independente de quando foram registradas.

### Query alternativa para despesas fixas mensais:

```java
// Todas as despesas FIXAS ativas (considerando que Repeat todo mês)
@Query("SELECT COALESCE(SUM(e.installmentAmount), 0) FROM Expenses e WHERE e.type = 'FIXO' AND e.active = true AND e.status = 'PENDENTE'")
BigDecimal sumPendingFixedExpenses();
```

---

## Arquivo de Referência - Estrutura Atual

### Revenue (Receitas)

- `type`: FIXO, AVULSO
- `status`: PENDENTE, RECEBIDO
- `registeredAt`: data de registro
- `amount`: valor

### Expenses (Despesas)

- `type`: PARCELADO, FIXO, AVULSO
- `status`: PENDENTE, PAGO
- `registeredAt`: data de registro
- `amount`: valor total
- `installmentAmount`: valor da parcela (para PARCELADO e FIXO)
- `currentInstallment`: parcela atual
- `totalInstallments`: total de parcelas
- `active`: se está ativa
- `completed`: se foi concluída
