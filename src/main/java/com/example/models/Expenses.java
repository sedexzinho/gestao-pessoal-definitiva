package com.example.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "despesas")
public class Expenses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String nome;

    @Column(name = "valor_pago", nullable = false)
    private BigDecimal valorPago;

    @Column(nullable = false)
    private String status;

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Category categoria;

    @Column(name = "dia_vencimento")
    private Integer diaVencimento;

    @Column(name = "ativa")
    private Boolean ativa = true;

    @Column(name = "parcelas_restantes")
    private Integer parcelasRestantes;

    @Column(name = "parcela_atual")
    private Integer parcelaAtual;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "valor_Parcela")
    private BigDecimal valorParcela;

    
   
    

}
