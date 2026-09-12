package br.com.keila.modules.produto.controller;

import br.com.keila.modules.produto.dto.CorResponse;
import br.com.keila.modules.produto.repository.CorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Cores são dados de referência (seed V7); expostas apenas para leitura pelos formulários de grade. */
@RestController
@RequestMapping("/api/v1/cores")
@RequiredArgsConstructor
public class CorController {

    private final CorRepository corRepository;

    @GetMapping
    public List<CorResponse> listar() {
        return corRepository.findAllByOrderByNomeAsc().stream()
                .map(c -> new CorResponse(c.getId(), c.getNome(), c.getHexCode()))
                .toList();
    }
}
