package br.com.keila.modules.venda.repository;

import br.com.keila.modules.venda.model.Devolucao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevolucaoRepository extends JpaRepository<Devolucao, Long> {
    List<Devolucao> findByVendaOrigemIdOrderByCreatedAtDesc(Long vendaOrigemId);

    List<Devolucao> findAllByOrderByCreatedAtDesc();
}
