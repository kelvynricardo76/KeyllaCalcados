package br.com.keila.modules.relatorio.controller;

import br.com.keila.modules.relatorio.dto.DashboardResponse;
import br.com.keila.modules.relatorio.dto.RelatorioVendasResponse;
import br.com.keila.modules.relatorio.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return relatorioService.dashboard();
    }

    @GetMapping("/vendas")
    public RelatorioVendasResponse relatorioVendas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.relatorioVendas(inicio, fim);
    }
}
