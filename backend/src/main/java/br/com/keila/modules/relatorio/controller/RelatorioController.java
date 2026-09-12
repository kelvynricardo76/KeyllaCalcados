package br.com.keila.modules.relatorio.controller;

import br.com.keila.modules.relatorio.dto.ClienteRankingResponse;
import br.com.keila.modules.relatorio.dto.DashboardResponse;
import br.com.keila.modules.relatorio.dto.FuncionarioRankingResponse;
import br.com.keila.modules.relatorio.dto.ProdutoParadoResponse;
import br.com.keila.modules.relatorio.dto.ProdutoVencendoResponse;
import br.com.keila.modules.relatorio.dto.RelatorioVendasResponse;
import br.com.keila.modules.relatorio.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/funcionarios")
    public List<FuncionarioRankingResponse> relatorioFuncionarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.relatorioFuncionarios(inicio, fim);
    }

    @GetMapping("/produtos-vencendo")
    public List<ProdutoVencendoResponse> produtosVencendo(@RequestParam(defaultValue = "30") int dias) {
        return relatorioService.produtosVencendo(dias);
    }

    @GetMapping("/clientes")
    public List<ClienteRankingResponse> relatorioClientes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.relatorioClientes(inicio, fim);
    }

    @GetMapping("/produtos-parados")
    public List<ProdutoParadoResponse> produtosParados(@RequestParam(defaultValue = "60") int dias) {
        return relatorioService.produtosParados(dias);
    }
}
