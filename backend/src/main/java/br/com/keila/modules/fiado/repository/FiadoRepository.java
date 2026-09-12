package br.com.keila.modules.fiado.repository;

import br.com.keila.modules.fiado.model.Fiado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiadoRepository extends JpaRepository<Fiado, Long> {
    List<Fiado> findAllByOrderByDataVencimentoAsc();
}
