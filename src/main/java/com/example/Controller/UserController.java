package com.example.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.models.User;
import com.example.repository.UserRepository;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // GET /api/users/me - Obter usuário atual
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        return userRepository.findById(1L)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("name", user.getName());
                    response.put("email", "user@example.com");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/users/me - Atualizar perfil do usuário
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> userData) {
        return userRepository.findById(1L)
                .map(user -> {
                    if (userData.containsKey("name")) {
                        user.setName(userData.get("name"));
                    }
                    User updated = userRepository.save(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", updated.getId());
                    response.put("name", updated.getName());
                    response.put("email", "user@example.com");

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
