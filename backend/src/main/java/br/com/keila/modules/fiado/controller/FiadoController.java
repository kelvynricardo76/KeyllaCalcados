package br.com.keila.modules.fiado.controller;

import br.com.keila.modules.fiado.dto.FiadoRequest;
import br.com.keila.modules.fiado.dto.FiadoResponse;
import br.com.keila.modules.fiado.dto.PagamentoRequest;
import br.com.keila.modules.fiado.dto.PagamentoResponse;
import br.com.keila.modules.fiado.service.FiadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fiados")
@RequiredArgsConstructor
public class FiadoController {

    private final FiadoService fiadoService;

    @GetMapping
    public List<FiadoResponse> listar() {
        return fiadoService.listar();
    }

    @GetMapping("/{id}/pagamentos")
    public List<PagamentoResponse> listarPagamentos(@PathVariable Long id) {
        return fiadoService.listarPagamentos(id);
    }

    @PostMapping
    public ResponseEntity<FiadoResponse> criar(@Valid @RequestBody FiadoRequest request) {
        return ResponseEntity.ok(fiadoService.criar(request));
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<FiadoResponse> registrarPagamento(@PathVariable Long id, @Valid @RequestBody PagamentoRequest request) {
        return ResponseEntity.ok(fiadoService.registrarPagamento(id, request));
    }
}
