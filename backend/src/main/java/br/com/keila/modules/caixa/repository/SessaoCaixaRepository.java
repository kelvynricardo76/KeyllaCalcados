package br.com.keila.modules.caixa.repository;

import br.com.keila.modules.caixa.model.SessaoCaixa;
import br.com.keila.modules.caixa.model.StatusSessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessaoCaixaRepository extends JpaRepository<SessaoCaixa, Long> {
    Optional<SessaoCaixa> findByCaixaIdAndStatus(Long caixaId, StatusSessao status);

    List<SessaoCaixa> findAllByOrderByDataAberturaDesc();
}
