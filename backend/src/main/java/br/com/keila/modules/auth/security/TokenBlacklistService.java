package br.com.keila.modules.auth.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blacklist de tokens JWT invalidados no logout, mantida em memória.
 *
 * Substitui o Redis para permitir rodar a aplicação sem nenhuma dependência
 * externa (só um processo Java + o arquivo H2). Suficiente para uma instância
 * única do backend; numa implantação com múltiplas instâncias, isso precisaria
 * voltar a ser um armazenamento compartilhado (Redis, banco, etc.).
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant expiraEm) {
        blacklist.put(jti, expiraEm);
    }

    public boolean isBlacklisted(String jti) {
        Instant expiraEm = blacklist.get(jti);
        if (expiraEm == null) {
            return false;
        }
        if (expiraEm.isBefore(Instant.now())) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }

    /** Remove tokens já expirados a cada hora para não acumular memória indefinidamente. */
    @Scheduled(fixedRate = 3_600_000)
    void limparExpirados() {
        Instant agora = Instant.now();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(agora));
    }
}
