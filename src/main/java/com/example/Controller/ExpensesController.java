package com.example.Controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dto.ExpensesDTO;
import com.example.service.ExpensesService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin("*")
public class ExpensesController {
    @Autowired
    private ExpensesService expensesService;
   
    @PostMapping("/add")
    public ResponseEntity<String> registrarDespesa(@RequestBody @Valid ExpensesDTO expensesDTO){
        expensesService.registrarGasto(expensesDTO);
        return ResponseEntity.ok("DespesaRegistrada");
    }

   
}
