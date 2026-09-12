package br.com.keila.modules.fornecedor.repository;

import br.com.keila.modules.fornecedor.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByOrderByDataCompraDesc();
}
