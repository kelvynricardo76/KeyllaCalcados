package br.com.keila.modules.produto.controller;

import br.com.keila.modules.produto.dto.MarcaRequest;
import br.com.keila.modules.produto.dto.MarcaResponse;
import br.com.keila.modules.produto.service.MarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    @GetMapping
    public List<MarcaResponse> listar() {
        return marcaService.listar();
    }

    @PostMapping
    public ResponseEntity<MarcaResponse> criar(@Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.ok(marcaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody MarcaRequest request) {
        return ResponseEntity.ok(marcaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<MarcaResponse> alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return ResponseEntity.ok(marcaService.alterarAtivo(id, valor));
    }
}
