package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.models.Expenses;
import com.example.models.Pagamentos;
import com.example.repository.ExpensesRepository;
import com.example.repository.PagamentoRepository;
import com.example.repository.UserRepository;

@Service
public class ScheduledPaymentService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ExpensesRepository expensesRepository;

    @Autowired
    private UserRepository userRepository;

    // Executa todo dia às 00:00
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processarVencimentosDiarios() {
        LocalDate hoje = LocalDate.now();
        int diaAtual = hoje.getDayOfMonth();

        // Buscar todas as despesas ativas que vencem hoje (têm dia de vencimento)
        List<Expenses> despesasVencendoHoje = expensesRepository.findByDiaVencimentoAndAtivaTrue(diaAtual);

        for (Expenses despesa : despesasVencendoHoje) {
            criarPagamentoParaDespesa(despesa, hoje);
        }
    }

    private void criarPagamentoParaDespesa(Expenses despesa, LocalDate dataPagamento) {
       


        boolean jaExiste = pagamentoRepository.findAll().stream()
                .anyMatch(p -> p.getDespesa().getId().equals(despesa.getId())
                        && p.getDataPagamento().isEqual(dataPagamento));

        if (jaExiste) {
            return; // Já foi processado
        }

        Pagamentos pagamento = new Pagamentos();
        pagamento.setDespesa(despesa);
        pagamento.setDataPagamento(dataPagamento);

        // Usar valorParcela se existir, senão valorPago
        BigDecimal valor = despesa.getValorParcela() != null ? despesa.getValorParcela() : despesa.getValorPago();
        pagamento.setValor(valor);

        // Status PAGO - afeta o saldo imediatamente
        pagamento.setStatus("PAGO");

        // Número da parcela = parcelaAtual + 1
        Integer parcelaAtual = despesa.getParcelaAtual() != null ? despesa.getParcelaAtual() : 0;
        Integer parcelasRestantes = despesa.getParcelasRestantes() != null ? despesa.getParcelasRestantes() : 0;
        pagamento.setNumeroParcela(parcelaAtual + 1);

        // Atualizar despesa: parcelaAtual + 1 e parcelasRestantes - 1
        despesa.setParcelaAtual(parcelaAtual + 1);
        despesa.setParcelasRestantes(parcelasRestantes - 1);

        // Se não houver mais parcelas, marcar como inativa
        if (despesa.getParcelasRestantes() <= 0) {
            despesa.setAtiva(false);
        }

        // Descontar do saldo do usuário
        descontarDoSaldo(valor);

        // Salvar pagamento e despesa
        pagamentoRepository.save(pagamento);
        expensesRepository.save(despesa);
    }

    private void descontarDoSaldo(BigDecimal valor) {
        com.example.models.User usuario = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        BigDecimal saldoAtual = usuario.getSalarioMensal();
        usuario.setSalarioMensal(saldoAtual.subtract(valor));
        userRepository.save(usuario);
    }
}
