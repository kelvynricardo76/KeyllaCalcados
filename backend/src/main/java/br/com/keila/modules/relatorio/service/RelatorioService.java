package br.com.keila.modules.relatorio.service;

import br.com.keila.modules.estoque.repository.EstoqueRepository;
import br.com.keila.modules.fiado.model.Fiado;
import br.com.keila.modules.fiado.model.StatusFiado;
import br.com.keila.modules.fiado.repository.FiadoRepository;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.repository.ProdutoRepository;
import br.com.keila.modules.relatorio.dto.DashboardResponse;
import br.com.keila.modules.relatorio.dto.FuncionarioRankingResponse;
import br.com.keila.modules.relatorio.dto.PontoVendaHora;
import br.com.keila.modules.relatorio.dto.ProdutoRankingResponse;
import br.com.keila.modules.relatorio.dto.ProdutoVencendoResponse;
import br.com.keila.modules.relatorio.dto.RelatorioVendasResponse;
import br.com.keila.modules.relatorio.dto.VendaResumoResponse;
import br.com.keila.modules.usuario.model.Usuario;
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
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ProdutoRepository produtoRepository;

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
        Instant inicioTs = inicio.atStartOfDay(ZONE).toInstant();
        Instant fimTs = fim.atTime(LocalTime.MAX).atZone(ZONE).toInstant();

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

        List<ItemVendaRepository.RankingProduto> ranking = itemVendaRepository.ranquearProdutos(inicioTs, fimTs);

        List<ProdutoRankingResponse> maisVendidos = ranking.stream()
                .map(r -> new ProdutoRankingResponse(r.getProdutoId(), r.getNome(), r.getQuantidade(), r.getTotal()))
                .limit(10)
                .toList();

        List<ProdutoRankingResponse> menosVendidos = ranking.stream()
                .sorted(Comparator.comparingLong(ItemVendaRepository.RankingProduto::getQuantidade))
                .map(r -> new ProdutoRankingResponse(r.getProdutoId(), r.getNome(), r.getQuantidade(), r.getTotal()))
                .limit(10)
                .toList();

        Set<Long> produtosComVenda = ranking.stream()
                .map(ItemVendaRepository.RankingProduto::getProdutoId)
                .collect(Collectors.toSet());
        List<String> produtosSemVenda = produtoRepository.findAllByOrderByNomeAsc().stream()
                .filter(Produto::isAtivo)
                .filter(p -> !produtosComVenda.contains(p.getId()))
                .map(Produto::getNome)
                .toList();

        return new RelatorioVendasResponse(inicio, fim, faturamentoTotal, vendas.size(), ticketMedio,
                resumoVendas, maisVendidos, menosVendidos, produtosSemVenda);
    }

    /** Ranking de vendas por funcionário (vendedor) no período — para controle de desempenho. */
    public List<FuncionarioRankingResponse> relatorioFuncionarios(LocalDate inicio, LocalDate fim) {
        Instant inicioTs = inicio.atStartOfDay(ZONE).toInstant();
        Instant fimTs = fim.atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        List<Venda> vendas = vendaRepository.findByStatusAndCreatedAtBetween(StatusVenda.FECHADA, inicioTs, fimTs);

        Map<Usuario, List<Venda>> porUsuario = vendas.stream().collect(Collectors.groupingBy(Venda::getUsuario));

        return porUsuario.entrySet().stream()
                .map(entry -> {
                    Usuario usuario = entry.getKey();
                    List<Venda> vendasDoUsuario = entry.getValue();
                    BigDecimal total = somarTotais(vendasDoUsuario);
                    BigDecimal ticket = vendasDoUsuario.isEmpty()
                            ? BigDecimal.ZERO
                            : total.divide(BigDecimal.valueOf(vendasDoUsuario.size()), 2, RoundingMode.HALF_UP);
                    return new FuncionarioRankingResponse(
                            usuario.getId(), usuario.getNome(), usuario.getPerfil().name(),
                            vendasDoUsuario.size(), total, ticket);
                })
                .sorted(Comparator.comparing(FuncionarioRankingResponse::valorTotal).reversed())
                .toList();
    }

    /** Produtos com data de validade vencendo nos próximos `dias` (ou já vencidos). */
    public List<ProdutoVencendoResponse> produtosVencendo(int dias) {
        LocalDate hoje = LocalDate.now(ZONE);
        LocalDate limite = hoje.plusDays(dias);

        return produtoRepository.findAllByOrderByNomeAsc().stream()
                .filter(Produto::isAtivo)
                .filter(p -> p.getDataValidade() != null && !p.getDataValidade().isAfter(limite))
                .sorted(Comparator.comparing(Produto::getDataValidade))
                .map(p -> new ProdutoVencendoResponse(
                        p.getId(), p.getNome(), p.getMarca() != null ? p.getMarca().getNome() : null,
                        p.getDataValidade(), java.time.temporal.ChronoUnit.DAYS.between(hoje, p.getDataValidade()),
                        p.getDataValidade().isBefore(hoje)))
                .toList();
    }

    private List<Venda> buscarVendasFechadasDoDia(LocalDate dia) {
        Instant inicio = dia.atStartOfDay(ZONE).toInstant();
        Instant fim = dia.atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        return vendaRepository.findByStatusAndCreatedAtBetween(StatusVenda.FECHADA, inicio, fim);
    }

    private BigDecimal somarTotais(List<Venda> vendas) {
        return vendas.stream().map(Venda::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PontoVendaHora> vendasPorHora(List<Venda> vendas) {
        Map<Integer, BigDecimal> porHora = vendas.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getCreatedAt().atZone(ZONE).getHour(),
                        Collectors.reducing(BigDecimal.ZERO, Venda::getValorTotal, BigDecimal::add)));

        List<PontoVendaHora> pontos = new ArrayList<>();
        for (int hora = 8; hora <= 22; hora++) {
            BigDecimal valor = porHora.getOrDefault(hora, BigDecimal.ZERO);
            pontos.add(new PontoVendaHora(String.format("%02dh", hora), valor));
        }
        return pontos;
    }
}
