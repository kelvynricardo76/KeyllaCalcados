package br.com.keila.modules.venda.controller;

import br.com.keila.modules.venda.dto.DevolucaoRequest;
import br.com.keila.modules.venda.dto.DevolucaoResponse;
import br.com.keila.modules.venda.service.DevolucaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendas/{vendaId}/devolucoes")
@RequiredArgsConstructor
public class DevolucaoController {

    private final DevolucaoService devolucaoService;

    @GetMapping
    public List<DevolucaoResponse> listar(@PathVariable Long vendaId) {
        return devolucaoService.listarPorVenda(vendaId);
    }

    @PostMapping
    public ResponseEntity<DevolucaoResponse> registrar(@PathVariable Long vendaId, @Valid @RequestBody DevolucaoRequest request) {
        return ResponseEntity.ok(devolucaoService.registrar(vendaId, request));
    }
}
