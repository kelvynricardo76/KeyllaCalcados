package br.com.keila.modules.estoque.controller;

import br.com.keila.modules.estoque.dto.AjusteEstoqueRequest;
import br.com.keila.modules.estoque.dto.EstoqueResponse;
import br.com.keila.modules.estoque.dto.MovimentacaoResponse;
import br.com.keila.modules.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    public List<EstoqueResponse> listar() {
        return estoqueService.listar();
    }

    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoResponse> listarMovimentacoes(@PathVariable Long id) {
        return estoqueService.listarMovimentacoes(id);
    }

    @PostMapping("/ajuste")
    public ResponseEntity<EstoqueResponse> ajustar(@Valid @RequestBody AjusteEstoqueRequest request) {
        return ResponseEntity.ok(estoqueService.ajustar(request));
    }
}
