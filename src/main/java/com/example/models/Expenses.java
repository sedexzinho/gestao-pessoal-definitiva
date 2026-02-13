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
  private String nome;

  @Column(nullable = false)
  private BigDecimal valorPago;

  @Column(nullable = false)
  private String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_parcela", nullable = true)
  private Parcel parcela;

  @Column(nullable = false)
  private LocalDate dataRegistro;

  @ManyToOne
  @JoinColumn(name = "id_categoria", nullable = false)
  private Category categoria;
    
}
