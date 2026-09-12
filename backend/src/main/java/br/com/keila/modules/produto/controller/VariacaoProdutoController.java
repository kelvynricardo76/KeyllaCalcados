package br.com.keila.modules.produto.controller;

import br.com.keila.modules.produto.dto.VariacaoRequest;
import br.com.keila.modules.produto.dto.VariacaoResponse;
import br.com.keila.modules.produto.service.VariacaoProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VariacaoProdutoController {

    private final VariacaoProdutoService variacaoService;

    @GetMapping("/api/v1/produtos/{produtoId}/variacoes")
    public List<VariacaoResponse> listarPorProduto(@PathVariable Long produtoId) {
        return variacaoService.listarPorProduto(produtoId);
    }

    @GetMapping("/api/v1/variacoes")
    public List<VariacaoResponse> listarTodas() {
        return variacaoService.listarTodasAtivas();
    }

    @PostMapping("/api/v1/produtos/{produtoId}/variacoes")
    public ResponseEntity<VariacaoResponse> criar(@PathVariable Long produtoId, @Valid @RequestBody VariacaoRequest request) {
        return ResponseEntity.ok(variacaoService.criar(produtoId, request));
    }

    @PatchMapping("/api/v1/variacoes/{id}/ativo")
    public ResponseEntity<VariacaoResponse> alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return ResponseEntity.ok(variacaoService.alterarAtivo(id, valor));
    }

    @DeleteMapping("/api/v1/variacoes/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        variacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
