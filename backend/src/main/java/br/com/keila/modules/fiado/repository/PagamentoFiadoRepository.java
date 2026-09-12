package br.com.keila.modules.fiado.repository;

import br.com.keila.modules.fiado.model.PagamentoFiado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagamentoFiadoRepository extends JpaRepository<PagamentoFiado, Long> {
    List<PagamentoFiado> findByFiadoIdOrderByDataPagamentoDescCreatedAtDesc(Long fiadoId);
}
