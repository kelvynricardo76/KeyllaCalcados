package br.com.keila.modules.loja.controller;

import br.com.keila.modules.loja.dto.LojaResponse;
import br.com.keila.modules.loja.repository.LojaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaRepository lojaRepository;

    @GetMapping
    public List<LojaResponse> listar() {
        return lojaRepository.findAllByOrderByNomeAsc().stream()
                .map(l -> new LojaResponse(l.getId(), l.getNome(), l.isAtivo()))
                .toList();
    }
}
