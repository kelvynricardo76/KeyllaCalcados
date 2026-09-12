package br.com.keila.modules.produto.controller;

import br.com.keila.modules.produto.dto.ProdutoRequest;
import br.com.keila.modules.produto.dto.ProdutoResponse;
import br.com.keila.modules.produto.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) String busca) {
        return produtoService.listar(busca);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<ProdutoResponse> alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return ResponseEntity.ok(produtoService.alterarAtivo(id, valor));
    }
}
