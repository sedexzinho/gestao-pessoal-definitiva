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
@Table(name = "expenses")
public class Expenses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expens_id")
    private Long id;

    @Column(name = "expens_ds_type", nullable = false)
    private String type;

    @Column(name = "expens_ds_name", nullable = false)
    private String name;

    @Column(name = "expens_vl_amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "expens_st_status", nullable = false)
    private String status;

    @Column(name = "expens_dt_registered_at", nullable = false)
    private LocalDate registeredAt;

    @ManyToOne
    @JoinColumn(name = "catego_id", nullable = false)
    private Category category;

    @Column(name = "expens_nr_due_day")
    private Integer dueDay;

    @Column(name = "expens_fl_active")
    private Boolean active = true;

    @Column(name = "expens_nr_current_installment")
    private Integer currentInstallment;

    @Column(name = "expens_nr_total_installments")
    private Integer totalInstallments;

    @Column(name = "expens_vl_installment_amount")
    private BigDecimal installmentAmount;

    @Column(name = "expens_dt_last_payment")
    private LocalDate lastPaymentDate;

    @Column(name = "expens_fl_completed")
    private Boolean completed = false;
}
