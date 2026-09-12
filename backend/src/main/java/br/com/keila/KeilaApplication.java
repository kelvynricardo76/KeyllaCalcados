package br.com.keila;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da API Keila Calçados.
 *
 * Habilitamos:
 * - @EnableCaching: cache via Redis (produtos, sessões de caixa)
 * - @EnableJpaAuditing: auditoria automática de createdAt/updatedAt
 * - @EnableScheduling: jobs agendados (expirar orçamentos, alertas de fiado vencido)
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableScheduling
public class KeilaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeilaApplication.class, args);
    }
}
