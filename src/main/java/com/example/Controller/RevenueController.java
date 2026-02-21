package com.example.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dto.RevenueDTO;
import com.example.models.Revenue;
import com.example.repository.RevenuesRepository;
import com.example.service.RevenueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/revenues")
@CrossOrigin("*")
public class RevenueController {
  @Autowired
  private RevenueService revenueService;

  @Autowired
  private RevenuesRepository revenueRepository;

  // LISTAR TODAS DESPESAS
  @GetMapping
  public ResponseEntity<List<Revenue>> listarTodas() {
    return ResponseEntity.ok(revenueRepository.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Revenue> listarReceitaPorID(@PathVariable Long id) {
    return revenueRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<String> registrarReceita(@RequestBody @Valid RevenueDTO dto) {
    revenueService.registrarReceitas(dto);
    return ResponseEntity.ok("Receita Registrada");
  }

  @PutMapping("/{id}")
  public ResponseEntity<Revenue> atualizarReceita(@PathVariable Long id, @RequestBody RevenueDTO dto) {
    return revenueRepository.findById(id)
        .map(revenue -> {
          revenue.setName(dto.nome());
          revenue.setAmount(dto.valor());
          revenue.setType(dto.tipo());
          if (dto.diaVencimento() != null) {
            revenue.setDueDay(dto.diaVencimento());
          }
          Revenue update = revenueRepository.save(revenue);
          return ResponseEntity.ok(update);
        }).orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluirReceita(@PathVariable Long id) {
    if (revenueRepository.existsById(id)) {
      revenueRepository.deleteById(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/summary")
  public ResponseEntity<Map<String, BigDecimal>> getSummary() {
    Map<String, BigDecimal> summary = Map.of(
        "totalRevenues", revenueRepository.sumAllAmounts(),
        "totalFixed", revenueRepository.sumFixedAmount(),
        "totalPending", revenueRepository.sumPendingAmount(),
        "totalMiscellaneous", revenueRepository.sumMiscellaneousAmount()
      );
         
        
    return ResponseEntity.ok(summary);
  }

}
