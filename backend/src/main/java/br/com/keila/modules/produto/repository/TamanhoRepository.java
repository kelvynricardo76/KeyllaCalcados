package br.com.keila.modules.produto.repository;

import br.com.keila.modules.produto.model.Tamanho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TamanhoRepository extends JpaRepository<Tamanho, Long> {
    List<Tamanho> findAllByOrderByOrdemAsc();
}
