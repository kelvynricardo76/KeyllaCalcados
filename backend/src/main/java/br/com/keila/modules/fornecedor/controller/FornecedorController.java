package br.com.keila.modules.fornecedor.controller;

import br.com.keila.modules.fornecedor.dto.FornecedorRequest;
import br.com.keila.modules.fornecedor.dto.FornecedorResponse;
import br.com.keila.modules.fornecedor.service.FornecedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public List<FornecedorResponse> listar() {
        return fornecedorService.listar();
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.ok(fornecedorService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FornecedorResponse> atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<FornecedorResponse> alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return ResponseEntity.ok(fornecedorService.alterarAtivo(id, valor));
    }
}
