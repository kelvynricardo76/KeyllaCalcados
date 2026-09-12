package br.com.keila.modules.caixa.controller;

import br.com.keila.modules.caixa.dto.*;
import br.com.keila.modules.caixa.service.CaixaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;

    @GetMapping("/api/v1/caixas")
    public List<CaixaResponse> listarCaixas() {
        return caixaService.listarCaixas();
    }

    @PostMapping("/api/v1/caixas/{caixaId}/sessoes")
    public ResponseEntity<SessaoResponse> abrirSessao(@PathVariable Long caixaId, @Valid @RequestBody AbrirSessaoRequest request) {
        return ResponseEntity.ok(caixaService.abrirSessao(caixaId, request));
    }

    @GetMapping("/api/v1/sessoes/{id}")
    public ResponseEntity<SessaoResponse> buscarSessao(@PathVariable Long id) {
        return ResponseEntity.ok(caixaService.buscarSessao(id));
    }

    @PostMapping("/api/v1/sessoes/{id}/fechar")
    public ResponseEntity<SessaoResponse> fecharSessao(@PathVariable Long id, @Valid @RequestBody FecharSessaoRequest request) {
        return ResponseEntity.ok(caixaService.fecharSessao(id, request));
    }

    @PostMapping("/api/v1/sessoes/{id}/movimentacoes")
    public ResponseEntity<MovimentacaoCaixaResponse> lancarMovimentacao(
            @PathVariable Long id, @Valid @RequestBody MovimentacaoCaixaRequest request) {
        return ResponseEntity.ok(caixaService.lancarMovimentacao(id, request));
    }

    @GetMapping("/api/v1/sessoes/{id}/movimentacoes")
    public List<MovimentacaoCaixaResponse> listarMovimentacoes(@PathVariable Long id) {
        return caixaService.listarMovimentacoes(id);
    }
}
