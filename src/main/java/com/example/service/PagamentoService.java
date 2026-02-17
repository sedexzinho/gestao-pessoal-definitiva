package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Dto.ExpensesDTO;
import com.example.models.Expenses;
import com.example.models.Pagamentos;
import com.example.models.User;
import com.example.repository.ExpensesRepository;
import com.example.repository.PagamentoRepository;
import com.example.repository.UserRepository;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ExpensesRepository expensesRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Unifica os pagamentos do dia: busca despesas que vencem hoje
     * e cria registros na tabela de pagamentos
     */
    @Transactional
    public void unificarPagamentosDoDia() {
        // Pegar o dia de hoje (dia do mês)
        int diaVencimentoHoje = LocalDate.now().getDayOfMonth();

        // Buscar despesas que vencem hoje e estão ativas
        List<Expenses> despesasDoDia = expensesRepository.findByDiaVencimentoAndAtivaTrue(diaVencimentoHoje);

        // Para cada despesa, criar um pagamento
        for (Expenses expenses : despesasDoDia) {
            processarPagamento(expenses);
        }
    }

    @Transactional
    public void processarPagamento(Expenses expenses) {
        BigDecimal valorParaCobrar;
        int parcelaAtualSendoPaga = expenses.getParcelaAtual() + 1;
        if (expenses.getParcelasRestantes() == 1) {
            BigDecimal valorJaPago = expenses.getValorParcela()
                    .multiply(BigDecimal.valueOf(expenses.getParcelaAtual()));
            valorParaCobrar = expenses.getValorPago().subtract(valorJaPago);
        } else {
            valorParaCobrar = expenses.getValorParcela();

        }
        // Define o nome do pagamento
        String nomePagamento;
        if (expenses.getTotalParcelas() != null && expenses.getTotalParcelas() > 1) {
            // Se for parcela, mostra o nome com informações da parcela
            nomePagamento = expenses.getNome() + " (" + parcelaAtualSendoPaga + "/" + expenses.getTotalParcelas() + ")";
        } else {
            // Se não for parcela, mostra só o nome
            nomePagamento = expenses.getNome();
        }

        Pagamentos newExpenses = new Pagamentos();
        newExpenses.setNome(nomePagamento);
        newExpenses.setTipo(expenses.getTipo());
        newExpenses.setValor(valorParaCobrar); // Aqui o valor entra no "extrato"
        newExpenses.setCategoria(expenses.getCategoria());
        newExpenses.setDataPagamento(LocalDate.now());
        newExpenses.setStatus("PAGO");
        newExpenses.setDespesa(expenses);
        pagamentoRepository.save(newExpenses);
    }

}
