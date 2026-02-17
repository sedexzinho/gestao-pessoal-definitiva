
package com.example.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Dto.ExpensesDTO;
import com.example.models.Category;
import com.example.models.Expenses;
import com.example.models.Pagamentos;
import com.example.models.User;
import com.example.repository.CategoryRepository;
import com.example.repository.ExpensesRepository;
import com.example.repository.PagamentoRepository;
import com.example.repository.UserRepository;

@Service
public class ExpensesService {

    private UserRepository userRepository;
    private ExpensesRepository expensesRepository;
    private CategoryRepository categoryRepository;
    private PagamentoRepository pagamentoRepository;

    public ExpensesService(
            UserRepository userRepository,
            ExpensesRepository expensesRepository,
            CategoryRepository categoryRepository,
            PagamentoRepository pagamentoRepository) {
        this.userRepository = userRepository;
        this.expensesRepository = expensesRepository;
        this.categoryRepository = categoryRepository;
        this.pagamentoRepository = pagamentoRepository;

    }

    public BigDecimal consultarSaldoSimples() {
        // 1. Se já é BigDecimal, o map apenas "deixa passar" o valor
        BigDecimal salario = userRepository.findById(1L)
                .map(u -> u.getSalarioMensal())
                .orElse(BigDecimal.ZERO);

        // 2. O total gasto (Stream de Expenses)
        BigDecimal totalGasto = expensesRepository.findAll().stream()
                .map(Expenses::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Agora a conta fecha perfeitamente
        return salario.subtract(totalGasto);
    }

    @Transactional
    public void registrarGasto(ExpensesDTO dto) {

        Category category = categoryRepository.findByNomeIgnoreCase(dto.nomeCategoria()).orElseGet(() -> {

            Category nova = new Category();
            nova.setNome(dto.nomeCategoria());
            return categoryRepository.save(nova);
        });

        Expenses expenses = mapToEntity(dto, category);
        expensesRepository.save(expenses);

        // Se for despesa simples (não PARCELADO e não FIXO), criar pagamento
        // imediatamente
        if (!"PARCELADO".equals(dto.tipo()) && !"FIXO".equals(dto.tipo())) {
            criarPagamentoSimples(expenses);
        }
    }

    private void criarPagamentoSimples(Expenses expenses) {
        // Criar pagamento com status PAGO
        Pagamentos pagamento = new Pagamentos();
        pagamento.setDespesa(expenses);
        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setValor(expenses.getValorPago());
        pagamento.setStatus("PAGO");
        pagamento.setNumeroParcela(1);
        pagamentoRepository.save(pagamento);

        // Descontar do saldo do usuário
        descontarDoSaldo(expenses.getValorPago());
    }

    private void descontarDoSaldo(BigDecimal valor) {
        User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BigDecimal saldoAtual = usuario.getSalarioMensal();
        usuario.setSalarioMensal(saldoAtual.subtract(valor));
        userRepository.save(usuario);
    }

    private Expenses mapToEntity(ExpensesDTO dto, Category category) {
        BigDecimal valorDaParcela = dto.valorPago().divide(
                BigDecimal.valueOf(dto.totalParcelas()), 2, RoundingMode.HALF_EVEN);
        Expenses expenses = new Expenses();
        expenses.setNome(dto.nome());
        expenses.setValorPago(dto.valorPago());
        expenses.setCategoria(category);
        expenses.setDataRegistro(LocalDate.now());
        expenses.setStatus("PAGO");
        expenses.setTipo(dto.tipo());
        expenses.setAtiva(dto.ativa());
        expenses.setDiaVencimento(dto.diaVencimento());
        expenses.setValorParcela(valorDaParcela);
        expenses.setParcelasRestantes(dto.parcelasRestantes());
        expenses.setParcelaAtual(dto.parcelaAtual());
        expenses.setTotalParcelas(dto.totalParcelas());
        return expenses;
    }

    public List<Expenses> findAll() {
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

}
