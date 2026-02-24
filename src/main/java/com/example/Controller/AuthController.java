package com.example.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.models.User;
import com.example.repository.UserRepository;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // POST /api/auth/login - Login de usuário
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Simples verificação - em produção usar BCrypt
        if (email != null && password != null && password.length() >= 6) {
            // Busca usuário ou cria um mock se não existir
            User user = userRepository.findById(1L).orElseGet(() -> {
                User newUser = new User();
                newUser.setName(email.split("@")[0]);
                newUser.setCode(1L);
                return userRepository.save(newUser);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", email);
            response.put("name", user.getName());
            response.put("token", "mock-token-" + System.currentTimeMillis());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("message", "Credenciais inválidas"));
    }

    // POST /api/auth/register - Registro de usuário
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userData) {
        User user = new User();
        user.setName(userData.get("name"));
        user.setCode(System.currentTimeMillis());

        User saved = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("email", userData.get("email"));
        response.put("name", saved.getName());
        response.put("token", "mock-token-" + System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
