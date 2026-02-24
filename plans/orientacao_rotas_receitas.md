# Orientações para Criar as Rotas de Receitas

## 1. Backend - Repository

Crie o arquivo `RevenueRepository.java` em `src/main/java/com/example/repository/`:

```java
package com.example.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.models.Revenue;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    // Buscar receitas por dia de vencimento e ativas
    List<Revenue> findByDueDayAndActiveTrue(Integer dueDay);

    // Buscar receitas ativas por status e tipo
    List<Revenue> findByActiveTrueAndStatusAndTypeIn(String status, List<String> types);

    // ===== REFATORAÇÃO JPQL =====

    /**
     * Buscar receitas por ano e mês usando JPQL.
     * VANTAGEM: Filtra no banco de dados, não carrega tudo para memória.
     */
    @Query("SELECT r FROM Revenue r WHERE YEAR(r.registeredAt) = :year AND MONTH(r.registeredAt) = :month")
    List<Revenue> findByMonth(int year, int month);

    /**
     * Somar todas as receitas usando JPQL.
     * VANTAGEM: Soma diretamente no banco, muito mais eficiente.
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r")
    BigDecimal sumAllAmounts();

    /**
     * Somar receitas do tipo FIXO.
     * ANTES: findAll().stream().filter(r -> "FIXO".equals(r.getType()))...
     * DEPOIS: sumFixedAmounts() - soma no banco!
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.type = 'FIXO'")
    BigDecimal sumFixedAmounts();

    /**
     * Somar receitas com status PENDENTE.
     * ANTES: findAll().stream().filter(r -> "PENDENTE".equals(r.getStatus()))...
     * DEPOIS: sumPendingAmounts() - soma no banco!
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.status = 'PENDENTE'")
    BigDecimal sumPendingAmounts();
}
```

> **Nota Importante:** As queries JPQL `findByMonth` e `sumAllAmounts` sãoMUITO mais eficientes que usar `findAll().stream().filter()` porque filtram e agregam diretamente no banco de dados, em vez de carregar todos os registros para a memória.

````

---

## 2. Backend - Service

Crie o arquivo `RevenueService.java` em `src/main/java/com/example/service/`:

**Nota:** Este service NÃO cria categorias automaticamente. O usuário deve criar a categoria primeiro no frontend (em Despesas ou na tela de Categorias).

```java
package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Dto.RevenueDTO;
import com.example.models.Category;
import com.example.models.Revenue;
import com.example.models.User;
import com.example.repository.CategoryRepository;
import com.example.repository.RevenueRepository;
import com.example.repository.UserRepository;

@Service
public class RevenueService {

    private UserRepository userRepository;
    private RevenueRepository revenueRepository;
    private CategoryRepository categoryRepository;

    public RevenueService(
            UserRepository userRepository,
            RevenueRepository revenueRepository,
            CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.revenueRepository = revenueRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void registrarReceita(RevenueDTO dto) {
        // Buscar categoria existente - se não existir, lança erro
        Category category = categoryRepository.findByNameIgnoreCase(dto.nomeCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada. Crie a categoria primeiro em Despesas ou na tela de Categorias."));

        Revenue revenue = mapToEntity(dto, category);
        revenueRepository.save(revenue);

        // Adicionar ao saldo apenas se o status for RECEBIDO
        if ("RECEBIDO".equals(revenue.getStatus())) {
            adicionarAoSaldo(revenue.getAmount());
        }
    }

    private void adicionarAoSaldo(BigDecimal value) {
        User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BigDecimal currentSalary = usuario.getMonthlySalary();
        usuario.setMonthlySalary(currentSalary.add(value));
        userRepository.save(usuario);
    }

    private Revenue mapToEntity(RevenueDTO dto, Category category) {
        Revenue revenue = new Revenue();
        revenue.setName(dto.nome());
        revenue.setAmount(dto.valor());
        revenue.setCategory(category);
        revenue.setRegisteredAt(LocalDate.now());
        revenue.setType(dto.tipo());
        revenue.setActive(dto.ativa() != null ? dto.ativa() : true);
        revenue.setDueDay(dto.diaVencimento());

        // Lógica de status:
        // - AVULSO: Sempre RECEBIDO imediatamente
        // - FIXO: Verifica se a data de recebimento já passou ou é hoje
        LocalDate dataRecebimento = dto.dataRecebimento();
        LocalDate hoje = LocalDate.now();

        if ("AVULSO".equals(dto.tipo())) {
            revenue.setStatus("RECEBIDO");
            revenue.setReceivedDate(hoje);
        } else if ("FIXO".equals(dto.tipo())) {
            if (dataRecebimento != null && !dataRecebimento.isAfter(hoje)) {
                // Data já passou ou é hoje → RECEBIDO
                revenue.setStatus("RECEBIDO");
                revenue.setReceivedDate(dataRecebimento);
            } else {
                // Data ainda não chegou → PENDENTE
                revenue.setStatus("PENDENTE");
            }
        }

        return revenue;
    }

    public List<Revenue> findAll() {
        return revenueRepository.findAll();
    }

    // ===== REFATORAÇÃO JPQL =====

    /**
     * Buscar receitas do mês específico.
     * ANTES: revenueRepository.findAll().stream().filter()
     * DEPOIS: revenueRepository.findByMonth() - muito mais eficiente!
     */
    public List<Revenue> findByMonth(int year, int month) {
        return revenueRepository.findByMonth(year, month);
    }

    /**
     * Calcular total de todas as receitas.
     * ANTES: revenueRepository.findAll().stream().map().reduce()
     * DEPOIS: revenueRepository.sumAllAmounts() - soma no banco!
     */
    public BigDecimal calcularTotalReceitas() {
        return revenueRepository.sumAllAmounts();
    }

    /**
     * Calcular total de receitas FIXO.
     * ANTES: findAll().stream().filter(r -> "FIXO".equals(r.getType()))...
     * DEPOIS: sumFixedAmounts() - soma no banco!
     */
    public BigDecimal calcularTotalFixo() {
        return revenueRepository.sumFixedAmounts();
    }

    /**
     * Calcular total de receitas PENDENTES.
     * ANTES: findAll().stream().filter(r -> "PENDENTE".equals(r.getStatus()))...
     * DEPOIS: sumPendingAmounts() - soma no banco!
     */
    public BigDecimal calcularTotalPendente() {
        return revenueRepository.sumPendingAmounts();
    }

    /**
     * Obter resumo completo de receitas.
     * Usa todas as queries otimizadas para buscar os totais.
     */
    public Map<String, BigDecimal> obterResumo() {
        return Map.of(
            "totalRevenues", calcularTotalReceitas(),
            "totalFixed", calcularTotalFixo(),
            "totalPending", calcularTotalPendente()
        );
    }
}
```

---

## 3. Backend - Controller

Crie o arquivo `RevenueController.java` em `src/main/java/com/example/Controller/`:

> **Nota Importante:** As rotas `/summary` e `/month/{year}/{month}` servem propósitos DIFERENTES:
> - `/summary` → retorna dados agregados (totais) para cards de dashboard
> - `/month/{year}/{month}` → retorna lista detalhada de receitas para tabelas
>
> **Por isso não devem ser unificadas!** Porém, ambas foram OTIMIZADAS para usar queries JPQL.

```java
package com.example.Controller;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Dto.RevenueDTO;
import com.example.models.Revenue;
import com.example.repository.RevenueRepository;
import com.example.service.RevenueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/revenues")
@CrossOrigin("*")
public class RevenueController {

    @Autowired
    private RevenueService revenueService;

    // OBS: O RevenueRepository não é mais necessário para as rotas /summary e /month
    // pois agora usamos o RevenueService que tem queries JPQL otimizadas

    // GET /api/revenues - Listar todas as receitas
    @GetMapping
    public ResponseEntity<List<Revenue>> listarTodasReceitas() {
        return ResponseEntity.ok(revenueRepository.findAll());
    }

    // GET /api/revenues/{id} - Buscar receita por ID
    @GetMapping("/{id}")
    public ResponseEntity<Revenue> buscarReceitaPorId(@PathVariable Long id) {
        return revenueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/revenues - Criar nova receita
    @PostMapping
    public ResponseEntity<String> registrarReceita(@RequestBody @Valid RevenueDTO revenueDTO) {
        revenueService.registrarReceita(revenueDTO);
        return ResponseEntity.ok("ReceitaRegistrada");
    }

    // PUT /api/revenues/{id} - Atualizar receita
    @PutMapping("/{id}")
    public ResponseEntity<Revenue> atualizarReceita(@PathVariable Long id, @RequestBody RevenueDTO revenueDTO) {
        return revenueRepository.findById(id)
                .map(revenue -> {
                    revenue.setName(revenueDTO.nome());
                    revenue.setAmount(revenueDTO.valor());
                    revenue.setType(revenueDTO.tipo());
                    if (revenueDTO.diaVencimento() != null) {
                        revenue.setDueDay(revenueDTO.diaVencimento());
                    }
                    Revenue updated = revenueRepository.save(revenue);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/revenues/{id} - Excluir receita
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirReceita(@PathVariable Long id) {
        if (revenueRepository.existsById(id)) {
            revenueRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/revenues/summary - Resumo de receitas (OTIMIZADO)
    // ANTES: findAll().stream().filter().map().reduce() - carrega tudo na memória
    // DEPOIS: Usa queries JPQL que somam diretamente no banco
    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary() {
        // Usa os métodos do service que fazem a soma no banco de dados
        Map<String, BigDecimal> summary = Map.of(
            "totalRevenues", revenueService.calcularTotalReceitas(),
            "totalFixed", revenueService.calcularTotalFixo(),
            "totalPending", revenueService.calcularTotalPendente()
        );
        return ResponseEntity.ok(summary);
    }

    // GET /api/revenues/month/{year}/{month} - Receitas por mês (OTIMIZADO)
    // ANTES: findAll().stream().filter() - carrega tudo na memória
    // DEPOIS: Usa query JPQL que filtra diretamente no banco
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<Revenue>> getRevenuesByMonth(@PathVariable int year, @PathVariable int month) {
        // Usa o método do service que filtra no banco de dados
        List<Revenue> revenues = revenueService.findByMonth(year, month);**NAO DEVERIA SER NO REPOSITORY**?
        return ResponseEntity.ok(revenues);
    }
}
```

---

## 4. Frontend - Routes (App.jsx)

Edite o arquivo `gestao-pessoal/src/App.jsx` para adicionar as rotas:

```jsx
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useState, createContext } from "react";
import Layout from "./components/layout/Layout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ExpensesList from "./pages/ExpensesList";
import ExpenseForm from "./pages/ExpenseForm";
import RevenueList from "./pages/RevenueList"; // NOVO
import RevenueForm from "./pages/RevenueForm"; // NOVO
import Installments from "./pages/Installments";
import Categories from "./pages/Categories";

export const AuthContext = createContext(null);

function App() {
  const [user, setUser] = useState(null);

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="expenses" element={<ExpensesList />} />
            <Route path="expenses/new" element={<ExpenseForm />} />
            <Route path="expenses/:id/edit" element={<ExpenseForm />} />
            <Route path="revenues" element={<RevenueList />} /> // NOVO
            <Route path="revenues/new" element={<RevenueForm />} /> // NOVO
            <Route path="revenues/:id/edit" element={<RevenueForm />} /> // NOVO
            <Route path="installments" element={<Installments />} />
            <Route path="categories" element={<Categories />} />
          </Route>
        </Routes>
      </Router>
    </AuthContext.Provider>
  );
}

export default App;
```

---

## 5. Frontend - Sidebar

Edite o arquivo `gestao-pessoal/src/components/layout/Sidebar.jsx` para adicionar o menu de Receitas:

```jsx
const menuItems = [
  {
    path: "/",
    label: "Dashboard",
    icon: (
      <svg
        className="h-5 w-5"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
        />
      </svg>
    ),
  },
  {
    path: "/expenses",
    label: "Despesas",
    icon: (
      <svg
        className="h-5 w-5"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"
        />
      </svg>
    ),
  },
  // NOVO: Item de Receitas
  {
    path: "/revenues",
    label: "Receitas",
    icon: (
      <svg
        className="h-5 w-5"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
    ),
  },
  {
    path: "/installments",
    label: "Parcelas",
    icon: (
      <svg
        className="h-5 w-5"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
        />
      </svg>
    ),
  },
  {
    path: "/categories",
    label: "Categorias",
    icon: (
      <svg
        className="h-5 w-5"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
        />
      </svg>
    ),
  },
];
```

---

## Fluxo do Usuário para Criar Receita

1. **Primeiro:** O usuário deve criar uma categoria em **Despesas** ou **Categorias**
2. **Depois:** O usuário pode criar uma receita selecionando essa categoria

Isso evita a criação automática de categorias e mantém o sistema mais controlado.

---

## Resumo dos Arquivos a Criar/Editar

### Backend (Java)

| Arquivo                | Ação  | Caminho                               |
| ---------------------- | ----- | ------------------------------------- |
| RevenueRepository.java | Criar | src/main/java/com/example/repository/ |
| RevenueService.java    | Criar | src/main/java/com/example/service/    |
| RevenueController.java | Criar | src/main/java/com/example/Controller/ |

### Frontend (React)

| Arquivo           | Ação   | Caminho                               |
| ----------------- | ------ | ------------------------------------- |
| revenueService.js | Criar  | gestao-pessoal/src/services/          |
| RevenueList.jsx   | Criar  | gestao-pessoal/src/pages/             |
| RevenueForm.jsx   | Criar  | gestao-pessoal/src/pages/             |
| App.jsx           | Editar | gestao-pessoal/src/                   |
| Sidebar.jsx       | Editar | gestao-pessoal/src/components/layout/ |

---

## Próximos Passos

1. Crie o RevenueRepository.java
2. Crie o RevenueService.java
3. Crie o RevenueController.java
4. Crie o revenueService.js no frontend
5. Crie as páginas RevenueList.jsx e RevenueForm.jsx
6. Atualize o App.jsx com as rotas
7. Atualize a Sidebar com o menu de Receitas
````
