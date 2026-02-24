package com.example.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.models.Category;
import com.example.repository.CategoryRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /api/categories - Listar todas as categorias
    @GetMapping
    public ResponseEntity<List<Category>> listarTodasCategorias() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    // GET /api/categories/{id} - Buscar categoria por ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> buscarCategoriaPorId(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories - Criar nova categoria
    @PostMapping
    public ResponseEntity<Category> criarCategoria(@RequestBody Map<String, String> payload) {
        Category category = new Category();
        category.setName(payload.get("name"));
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/categories/{id} - Atualizar categoria
    @PutMapping("/{id}")
    public ResponseEntity<Category> atualizarCategoria(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return categoryRepository.findById(id)
            .map(category -> {
                category.setName(payload.get("name"));
                Category updated = categoryRepository.save(category);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/categories/{id} - Excluir categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCategoria(@PathVariable Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
