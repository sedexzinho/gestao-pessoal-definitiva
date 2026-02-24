package com.example.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Permitir origens específicas
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "https://gestao-pessoal-definitiva.vercel.app",
                "http://localhost:5173",
                "http://localhost:3000"));

        // Permitir todos os métodos HTTP
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permitir todos os headers
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));

        // Permitir credenciais
        corsConfiguration.setAllowCredentials(true);

        // Expor headers
        corsConfiguration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
