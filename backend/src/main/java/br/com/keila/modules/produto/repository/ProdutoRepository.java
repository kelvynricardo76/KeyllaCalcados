package br.com.keila.modules.produto.repository;

import br.com.keila.modules.produto.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAllByOrderByNomeAsc();

    List<Produto> findByNomeContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByNomeAsc(
            String nome, String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
