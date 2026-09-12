package br.com.keila;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da API Keila Calçados.
 *
 * Habilitamos:
 * - @EnableJpaAuditing: auditoria automática de createdAt/updatedAt
 * - @EnableScheduling: jobs agendados (limpeza da blacklist de JWT, alertas futuros)
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class KeilaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeilaApplication.class, args);
    }
}
