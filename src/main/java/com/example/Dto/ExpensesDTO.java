package com.example.Dto;

import java.math.BigDecimal;
import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpensesDTO(
    @NotBlank(message = "Nome não pode ser vazio") 
    String nome,

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor deve ser positivo")
    BigDecimal valorPago,

    @NotBlank(message = "A categoria é obrigatória")
    String nomeCategoria,

    @NotNull(message = "A quantidade de parcelas é obrigatória")
    @Min(value = 1, message = "O numero de parcelas deve ser ao menos 1")
    Integer qtdParcelas,

    @NotNull(message = "O dia de vencimento é obrigatório")
    @Range(min = 1, max = 31, message = "Dia de vencimento inválido")
    Integer diaVencimento
) {
    // A lógica do isParcelado entra aqui dentro do corpo do record
    public boolean isParcelado() {
        return qtdParcelas != null && qtdParcelas > 1;
    }
}