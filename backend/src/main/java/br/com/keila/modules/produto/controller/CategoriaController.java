package br.com.keila.modules.produto.controller;

import br.com.keila.modules.produto.dto.CategoriaRequest;
import br.com.keila.modules.produto.dto.CategoriaResponse;
import br.com.keila.modules.produto.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<CategoriaResponse> alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return ResponseEntity.ok(categoriaService.alterarAtivo(id, valor));
    }
}
