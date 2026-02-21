package com.example.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueDTO(
                String nome,
                String tipo,
                BigDecimal valor,
                String nomeCategoria,
                Integer diaVencimento,
                Boolean ativa,
                LocalDate dataRecebimento) {
}
