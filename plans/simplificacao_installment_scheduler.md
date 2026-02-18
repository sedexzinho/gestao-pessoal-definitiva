# Análise de Simplificação - InstallmentSchedulerService

## Visão Geral

O `InstallmentSchedulerService` possui ~153 linhas. Esta que pode análise identifica o ser simplificado mantendo a lógica de negócio correta.

---

## Problemas Identificados

### 1. Validações redundantes em `canBePaid()` ✅ PODE SIMPLIFICAR

**Arquivo:** `src/main/java/com/example/service/InstallmentSchedulerService.java:98-120`

O método verifica condições que já são garantidas pela query do repository:

```java
private boolean canBePaid(Expenses expense) {
    // ❌ redundante - query já filtra por tipo
    if (!RECURRING_TYPES.contains(expense.getType())) {
        return false;
    }
    // ❌ redundante - query já filtra active=true
    if (!Boolean.TRUE.equals(expense.getActive())) {
        return false;
    }
    // ✅ necessária - pode não ter valor de parcela
    if (expense.getInstallmentAmount() == null || expense.getInstallmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
        return false;
    }
    // ❌ redundante - query já filtra status="PENDENTE"
    if (!"PENDENTE".equals(expense.getStatus())) {
        return false;
    }
    return true;
}
```

**Solução:** Remover validações redundantes e manter apenas a verificação de `installmentAmount`.

---

### 2. If/else redundante em `descontarDoSaldo()` ✅ PODE SIMPLIFICAR

**Arquivo:** `src/main/java/com/example/service/InstallmentSchedulerService.java:125-141`

```java

if (currentBalance.compareTo(value) < 0) {
    usuario.setMonthlySalary(currentBalance.subtract(value));
} else {
    usuario.setMonthlySalary(currentBalance.subtract(value));
}
```

**Solução:** Remover o if/else - sempre subtrai o valor.

---

### 3. Duplicação do método `descontarDoSaldo()` ✅ PODE SIMPLIFICAR

Existe o **mesmo método** em dois arquivos:

| Arquivo                            | Linha |
| ---------------------------------- | ----- |
| `ExpensesService.java`             | 62    |
| `InstallmentSchedulerService.java` | 125   |

**Solução:** Criar um serviço compartilhado `BalanceService` ou usar injeção de dependência. **nao precisa criar um arquivo, pode reutilizar de ExpensesService**

---

### 4. Query sem filtro de dia na primeira operação ❌ CORRETO MANTER

A primeira query é **necessária** para o ciclo de negócio:

```
17/02: Usuário registra parcela vencimento 18/02 → PENDENTE
18/02: Usuário paga → PAGO
01/03: Scheduler roda → PAGO → PENDENTE (preparar novo ciclo)
18/03: Scheduler roda → PENDENTE → PAGA
```

A lógica PAGO → PENDENTE é necessária para permitir novo pagamento no mês seguinte.

---

## Código Simplificado Proposto

```java
@Service
publicService {

    private class InstallmentScheduler final ExpensesRepository expensesRepository;
    private final UserRepository userRepository;

    private static final List<String> RECURRING_TYPES = Arrays.asList("PARCELADO", "FIXO");

    public InstallmentSchedulerService(ExpensesRepository expensesRepository, UserRepository userRepository) {
        this.expensesRepository = expensesRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void processMonthlyPayments() {
        int today = LocalDate.now().getDayOfMonth();

        // Primeira etapa: PAGO → PENDENTE para despesas não concluídas (NECESSÁRIO)
        List<Expenses> paidExpenses = expensesRepository
                .findByActiveTrueAndStatusAndTypeIn("PAGO", RECURRING_TYPES);

        for (Expenses expense : paidExpenses) {
            if (!Boolean.TRUE.equals(expense.getCompleted())) {
                expense.setStatus("PENDENTE");
                expensesRepository.save(expense);
            }
        }

        // Segunda etapa: Processa despesas PENDENTE do dia
        List<Expenses> pendingExpenses = expensesRepository
                .findByActiveTrueAndStatusAndTypeInAndDueDay("PENDENTE", RECURRING_TYPES, today);

        for (Expenses expense : pendingExpenses) {
            processPayment(expense);
        }
    }

    @Transactional
    public void processPayment(Expenses expense) {
        // ✅ Validação mínima necessária - verifica valor da parcela
        if (expense.getInstallmentAmount() == null ||
            expense.getInstallmentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Desconta do saldo
        descontarDoSaldo(expense.getInstallmentAmount());

        // Atualiza dados do pagamento
        expense.setLastPaymentDate(LocalDate.now());

        int currentInstallment = expense.getCurrentInstallment() != null
                ? expense.getCurrentInstallment()
                : 0;
        expense.setCurrentInstallment(currentInstallment + 1);

        // Verifica se é a última parcela
        if (expense.getCurrentInstallment() >= expense.getTotalInstallments()) {
            expense.setCompleted(true);
            expense.setActive(false);
        }

        expense.setStatus("PAGO");
        expensesRepository.save(expense);
    }

    private void descontarDoSaldo(BigDecimal value) {
        User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // ✅ Simplificado - sempre subtrai
        usuario.setMonthlySalary(usuario.getMonthlySalary().subtract(value));
        userRepository.save(usuario);
    }

    @Transactional
    public void processPaymentManual(Long expenseId) {
        Expenses expense = expensesRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        processPayment(expense);
    }
}
```

---

## Resumo das Simplificações

| Item             | Antes              | Depois      | Status           |
| ---------------- | ------------------ | ----------- | ---------------- |
| canBePaid        | 5 validações       | 1 validação | ✅ Simplificável |
| descontarDoSaldo | if/else redundante | 1 linha     | ✅ Simplificável |
| PAGO→PENDENTE    | Mantido            | Mantido     | ❌ Necessário    |
| Duplicação       | 2 métodos          | 1 serviço   | ✅ Refatorar     |

---

## Recomendações

1. **Simplificar canBePaid** - Remover validações redundantes
2. **Simplificar descontarDoSaldo** - Remover if/else
3. **Criar BalanceService** - Extrair método duplicado para serviço compartilhado
4. **Manter lógica PAGO→PENDENTE** - É necessária para o ciclo de negócio

Código ficaria ~40% menor e mais limpo, mantendo toda a funcionalidade.
