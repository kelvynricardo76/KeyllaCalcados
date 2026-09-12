package br.com.keila.modules.relatorio.service;

import br.com.keila.modules.estoque.repository.EstoqueRepository;
import br.com.keila.modules.fiado.model.Fiado;
import br.com.keila.modules.fiado.model.StatusFiado;
import br.com.keila.modules.fiado.repository.FiadoRepository;
import br.com.keila.modules.relatorio.dto.DashboardResponse;
import br.com.keila.modules.relatorio.dto.PontoVendaHora;
import br.com.keila.modules.relatorio.dto.ProdutoRankingResponse;
import br.com.keila.modules.relatorio.dto.RelatorioVendasResponse;
import br.com.keila.modules.relatorio.dto.VendaResumoResponse;
import br.com.keila.modules.venda.model.StatusVenda;
import br.com.keila.modules.venda.model.Venda;
import br.com.keila.modules.venda.repository.ItemVendaRepository;
import br.com.keila.modules.venda.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final VendaRepository vendaRepository;
    private final FiadoRepository fiadoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemVendaRepository itemVendaRepository;

    public DashboardResponse dashboard() {
        LocalDate hoje = LocalDate.now(ZONE);
        LocalDate ontem = hoje.minusDays(1);

        List<Venda> vendasHoje = buscarVendasFechadasDoDia(hoje);
        List<Venda> vendasOntem = buscarVendasFechadasDoDia(ontem);

        BigDecimal faturamentoHoje = somarTotais(vendasHoje);
        BigDecimal faturamentoOntem = somarTotais(vendasOntem);
        BigDecimal ticketMedio = vendasHoje.isEmpty()
                ? BigDecimal.ZERO
                : faturamentoHoje.divide(BigDecimal.valueOf(vendasHoje.size()), 2, RoundingMode.HALF_UP);

        List<Fiado> fiados = fiadoRepository.findAllByOrderByDataVencimentoAsc();
        BigDecimal fiadosEmAberto = fiados.stream()
                .filter(f -> f.getStatus() != StatusFiado.PAGO)
                .map(Fiado::getValorRestante)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long fiadosVencidos = fiados.stream()
                .filter(f -> f.getStatus() != StatusFiado.PAGO && f.getDataVencimento().isBefore(hoje))
                .count();

        long estoqueBaixo = estoqueRepository.findAll().stream()
                .filter(e -> e.getQuantidade() <= e.getEstoqueMinimo())
                .count();

        return new DashboardResponse(
                faturamentoHoje, faturamentoOntem, vendasHoje.size(), ticketMedio,
                fiadosEmAberto, fiadosVencidos, estoqueBaixo, vendasPorHora(vendasHoje));
    }

    public RelatorioVendasResponse relatorioVendas(LocalDate inicio, LocalDate fim) {
        OffsetDateTime inicioTs = inicio.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime fimTs = fim.atTime(LocalTime.MAX).atZone(ZONE).toOffsetDateTime();

        List<Venda> vendas = vendaRepository.findByStatusAndCreatedAtBetween(StatusVenda.FECHADA, inicioTs, fimTs);
        BigDecimal faturamentoTotal = somarTotais(vendas);
        BigDecimal ticketMedio = vendas.isEmpty()
                ? BigDecimal.ZERO
                : faturamentoTotal.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);

        List<VendaResumoResponse> resumoVendas = vendas.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(v -> new VendaResumoResponse(
                        v.getId(), v.getCreatedAt(),
                        v.getCliente() != null ? v.getCliente().getNome() : "Consumidor final",
                        v.getUsuario().getNome(), v.getValorTotal()))
                .toList();

        List<ProdutoRankingResponse> topProdutos = itemVendaRepository.ranquearProdutos(inicioTs, fimTs).stream()
                .map(r -> new ProdutoRankingResponse(r.getNome(), r.getQuantidade(), r.getTotal()))
                .limit(10)
                .toList();

        return new RelatorioVendasResponse(inicio, fim, faturamentoTotal, vendas.size(), ticketMedio, resumoVendas, topProdutos);
    }

    private List<Venda> buscarVendasFechadasDoDia(LocalDate dia) {
        OffsetDateTime inicio = dia.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime fim = dia.atTime(LocalTime.MAX).atZone(ZONE).toOffsetDateTime();
        return vendaRepository.findByStatusAndCreatedAtBetween(StatusVenda.FECHADA, inicio, fim);
    }

    private BigDecimal somarTotais(List<Venda> vendas) {
        return vendas.stream().map(Venda::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PontoVendaHora> vendasPorHora(List<Venda> vendas) {
        Map<Integer, BigDecimal> porHora = vendas.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getCreatedAt().atZoneSameInstant(ZONE).getHour(),
                        Collectors.reducing(BigDecimal.ZERO, Venda::getValorTotal, BigDecimal::add)));

        List<PontoVendaHora> pontos = new ArrayList<>();
        for (int hora = 8; hora <= 22; hora++) {
            BigDecimal valor = porHora.getOrDefault(hora, BigDecimal.ZERO);
            pontos.add(new PontoVendaHora(String.format("%02dh", hora), valor));
        }
        return pontos;
    }
}
