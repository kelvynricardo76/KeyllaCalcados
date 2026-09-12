package br.com.keila.modules.fornecedor.controller;

import br.com.keila.modules.fornecedor.dto.CompraRequest;
import br.com.keila.modules.fornecedor.dto.CompraResponse;
import br.com.keila.modules.fornecedor.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public List<CompraResponse> listar() {
        return compraService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompraResponse> criar(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.ok(compraService.criar(request));
    }

    @PostMapping("/{id}/receber")
    public ResponseEntity<CompraResponse> receber(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.receber(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CompraResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.cancelar(id));
    }
}
