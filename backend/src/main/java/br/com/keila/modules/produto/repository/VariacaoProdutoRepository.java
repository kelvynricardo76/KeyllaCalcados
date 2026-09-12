package br.com.keila.modules.produto.repository;

import br.com.keila.modules.produto.model.VariacaoProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariacaoProdutoRepository extends JpaRepository<VariacaoProduto, Long> {
    List<VariacaoProduto> findByProdutoIdOrderByTamanhoOrdemAscCorNomeAsc(Long produtoId);
    boolean existsBySku(String sku);
}
