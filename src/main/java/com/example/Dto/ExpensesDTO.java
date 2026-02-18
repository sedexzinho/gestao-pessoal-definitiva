package com.example.Dto;

import java.math.BigDecimal;
import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpensesDTO(
        @NotBlank(message = "Nome não pode ser vazio") String nome,

        @NotBlank(message = "Selecione o tipo da despesa") String tipo,

        @NotNull(message = "O valor é obrigatório") @Positive(message = "O valor deve ser positivo") BigDecimal valorPago,

        @NotBlank(message = "A categoria é obrigatória") String nomeCategoria,

        @Min(value = 1, message = "O numero de parcelas deve ser ao menos 1") Integer totalParcelas,
        BigDecimal valorParcela,
        Integer parcelaAtual,
        Integer parcelasRestantes,
        Boolean ativa,

        @Range(min = 1, max = 31, message = "Dia de vencimento inválido") Integer diaVencimento)

{

    public boolean isParcelado() {
        return totalParcelas != null && totalParcelas > 1 && "PARCELADO".equals(tipo);
    }

    public boolean isFixo() {
        return "FIXO".equals(tipo);
    }

}