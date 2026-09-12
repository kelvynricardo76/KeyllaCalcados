package br.com.keila.modules.venda.controller;

import br.com.keila.modules.venda.dto.*;
import br.com.keila.modules.venda.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @GetMapping
    public List<VendaResponse> listarPorSessao(@RequestParam Long sessaoId) {
        return vendaService.listarPorSessao(sessaoId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VendaResponse> abrir(@Valid @RequestBody AbrirVendaRequest request) {
        return ResponseEntity.ok(vendaService.abrir(request));
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<VendaResponse> adicionarItem(@PathVariable Long id, @Valid @RequestBody AdicionarItemRequest request) {
        return ResponseEntity.ok(vendaService.adicionarItem(id, request));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public ResponseEntity<VendaResponse> removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(vendaService.removerItem(id, itemId));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<VendaResponse> finalizar(@PathVariable Long id, @Valid @RequestBody FinalizarVendaRequest request) {
        return ResponseEntity.ok(vendaService.finalizar(id, request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<VendaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.cancelar(id));
    }
}
