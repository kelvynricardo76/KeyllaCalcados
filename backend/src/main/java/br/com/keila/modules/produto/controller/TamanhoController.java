package br.com.keila.modules.produto.controller;

import br.com.keila.modules.produto.dto.TamanhoResponse;
import br.com.keila.modules.produto.repository.TamanhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Tamanhos são dados de referência (seed V7); expostos apenas para leitura pelos formulários de grade. */
@RestController
@RequestMapping("/api/v1/tamanhos")
@RequiredArgsConstructor
public class TamanhoController {

    private final TamanhoRepository tamanhoRepository;

    @GetMapping
    public List<TamanhoResponse> listar() {
        return tamanhoRepository.findAllByOrderByOrdemAsc().stream()
                .map(t -> new TamanhoResponse(t.getId(), t.getValor(), t.getTipo(), t.getOrdem()))
                .toList();
    }
}
