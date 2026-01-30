package com.service;

import java.math.BigDecimal;

import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
@Service
public class DespesasService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private DespesasRepository despesasRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    @Lazy
    private ParcelaService parcelaService;
    
      public BigDecimal consultarSaldoSimples() {
        // Busca o seu salário (ID 1)
        BigDecimal salario = usuarioRepository.findById(1L)
                .map(Usuario::getSalarioMensal)
                .orElse(BigDecimal.ZERO);

        // Soma todas as despesas
        BigDecimal totalGasto = despesasRepository.findAll().stream()
                .map(Despesas::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return salario.subtract(totalGasto);
    }
}
