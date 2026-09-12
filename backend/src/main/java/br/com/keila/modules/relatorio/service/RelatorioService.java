package br.com.keila.modules.relatorio.service;

import br.com.keila.modules.caixa.model.MovimentacaoCaixa;
import br.com.keila.modules.caixa.model.TipoMovCaixa;
import br.com.keila.modules.caixa.repository.MovimentacaoCaixaRepository;
import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.estoque.model.Estoque;
import br.com.keila.modules.estoque.repository.EstoqueRepository;
import br.com.keila.modules.fiado.model.Fiado;
import br.com.keila.modules.fiado.model.StatusFiado;
import br.com.keila.modules.fiado.repository.FiadoRepository;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.repository.ProdutoRepository;
import br.com.keila.modules.relatorio.dto.ClienteRankingResponse;
import br.com.keila.modules.relatorio.dto.DashboardResponse;
import br.com.keila.modules.relatorio.dto.FuncionarioRankingResponse;
import br.com.keila.modules.relatorio.dto.ItemVendaResumo;
import br.com.keila.modules.relatorio.dto.PagamentoResumo;
import br.com.keila.modules.relatorio.dto.PontoVendaHora;
import br.com.keila.modules.relatorio.dto.ProdutoParadoResponse;
import br.com.keila.modules.relatorio.dto.ProdutoRankingResponse;
import br.com.keila.modules.relatorio.dto.ProdutoVencendoResponse;
import br.com.keila.modules.relatorio.dto.RelatorioFinanceiroResponse;
import br.com.keila.modules.relatorio.dto.RelatorioVendasResponse;
import br.com.keila.modules.relatorio.dto.VendaDetalheResponse;
import br.com.keila.modules.relatorio.dto.VendaResumoResponse;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.venda.model.StatusVenda;
import br.com.keila.modules.venda.model.TipoDevolucao;
import br.com.keila.modules.venda.model.Venda;
import br.com.keila.modules.venda.repository.DevolucaoRepository;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    private final DevolucaoRepository devolucaoRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
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

        List<VendaDetalheResponse> ultimasVendas = vendasHoje.stream()
                .sorted(Comparator.comparing(Venda::getCreatedAt).reversed())
                .limit(15)
                .map(this::toVendaDetalhe)
                .toList();

        return new DashboardResponse(
                faturamentoHoje, faturamentoOntem, vendasHoje.size(), ticketMedio,
                fiadosEmAberto, fiadosVencidos, estoqueBaixo, vendasPorHora(vendasHoje), ultimasVendas);
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

    /** Ranking de clientes por total comprado no período — para identificar quem mais compra e o maior ticket médio. */
    public List<ClienteRankingResponse> relatorioClientes(LocalDate inicio, LocalDate fim) {
        Instant inicioTs = inicio.atStartOfDay(ZONE).toInstant();
        Instant fimTs = fim.atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        List<Venda> vendas = vendaRepository.findByStatusAndCreatedAtBetween(StatusVenda.FECHADA, inicioTs, fimTs);

        Map<Cliente, List<Venda>> porCliente = vendas.stream()
                .filter(v -> v.getCliente() != null)
                .collect(Collectors.groupingBy(Venda::getCliente));

        return porCliente.entrySet().stream()
                .map(entry -> {
                    Cliente cliente = entry.getKey();
                    List<Venda> vendasDoCliente = entry.getValue();
                    BigDecimal total = somarTotais(vendasDoCliente);
                    BigDecimal ticket = vendasDoCliente.isEmpty()
                            ? BigDecimal.ZERO
                            : total.divide(BigDecimal.valueOf(vendasDoCliente.size()), 2, RoundingMode.HALF_UP);
                    return new ClienteRankingResponse(
                            cliente.getId(), cliente.getNome(), cliente.getTelefone(), vendasDoCliente.size(), total, ticket);
                })
                .sorted(Comparator.comparing(ClienteRankingResponse::valorTotal).reversed())
                .toList();
    }

    /**
     * Demonstrativo financeiro simplificado do período: do faturamento bruto ao lucro líquido.
     * CMV usa o preço de custo ATUAL do produto (não há snapshot histórico de custo por venda),
     * então é uma aproximação — suficiente para acompanhamento gerencial do dia a dia.
     */
    public RelatorioFinanceiroResponse relatorioFinanceiro(LocalDate inicio, LocalDate fim) {
        Instant inicioTs = inicio.atStartOfDay(ZONE).toInstant();
        Instant fimTs = fim.atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        OffsetDateTime inicioOd = inicio.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime fimOd = fim.atTime(LocalTime.MAX).atZone(ZONE).toOffsetDateTime();

        List<Venda> vendas = vendaRepository.findByStatusAndCreatedAtBetween(StatusVenda.FECHADA, inicioTs, fimTs);

        BigDecimal faturamentoBruto = vendas.stream().map(Venda::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descontos = vendas.stream().map(Venda::getDescontoGeral).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cmv = vendas.stream()
                .flatMap(v -> v.getItens().stream())
                .map(i -> i.getProduto().getPrecoCusto().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal devolucoes = devolucaoRepository.findByCreatedAtBetween(inicioOd, fimOd).stream()
                .filter(d -> d.getTipo() == TipoDevolucao.DEVOLUCAO)
                .map(d -> d.getValorDevolvido())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal faturamentoLiquido = faturamentoBruto.subtract(descontos).subtract(devolucoes);
        BigDecimal lucroBruto = faturamentoLiquido.subtract(cmv);
        BigDecimal margemBruta = margemPercentual(lucroBruto, faturamentoLiquido);

        List<MovimentacaoCaixa> movimentacoes = movimentacaoCaixaRepository.findByCreatedAtBetween(inicioOd, fimOd);
        BigDecimal despesas = movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovCaixa.DESPESA)
                .map(MovimentacaoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receitasAvulsas = movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovCaixa.RECEITA_AVULSA)
                .map(MovimentacaoCaixa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lucroLiquido = lucroBruto.add(receitasAvulsas).subtract(despesas);
        BigDecimal margemLiquida = margemPercentual(lucroLiquido, faturamentoLiquido);

        BigDecimal ticketMedio = vendas.isEmpty()
                ? BigDecimal.ZERO
                : faturamentoBruto.divide(BigDecimal.valueOf(vendas.size()), 2, RoundingMode.HALF_UP);

        return new RelatorioFinanceiroResponse(inicio, fim, vendas.size(), ticketMedio,
                faturamentoBruto, descontos, devolucoes, faturamentoLiquido, cmv, lucroBruto, margemBruta,
                despesas, receitasAvulsas, lucroLiquido, margemLiquida);
    }

    private BigDecimal margemPercentual(BigDecimal valor, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return valor.divide(base, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    /** Produtos ativos, com estoque disponível, sem venda há pelo menos `dias` dias (ou nunca vendidos). */
    public List<ProdutoParadoResponse> produtosParados(int dias) {
        LocalDate hoje = LocalDate.now(ZONE);

        Map<Long, Instant> ultimaVendaPorProduto = itemVendaRepository.buscarUltimaVendaPorProduto().stream()
                .collect(Collectors.toMap(
                        ItemVendaRepository.UltimaVendaProduto::getProdutoId,
                        ItemVendaRepository.UltimaVendaProduto::getUltimaVenda));

        Map<Long, Integer> estoquePorProduto = estoqueRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getVariacao().getProduto().getId(),
                        Collectors.summingInt(Estoque::getQuantidade)));

        return produtoRepository.findAllByOrderByNomeAsc().stream()
                .filter(Produto::isAtivo)
                .filter(p -> estoquePorProduto.getOrDefault(p.getId(), 0) > 0)
                .map(p -> {
                    Instant ultima = ultimaVendaPorProduto.get(p.getId());
                    Long diasSemVenda = ultima != null
                            ? ChronoUnit.DAYS.between(ultima.atZone(ZONE).toLocalDate(), hoje)
                            : null;
                    return new ProdutoParadoResponse(
                            p.getId(), p.getNome(), p.getMarca() != null ? p.getMarca().getNome() : null,
                            ultima, diasSemVenda, estoquePorProduto.getOrDefault(p.getId(), 0));
                })
                .filter(r -> r.diasSemVenda() == null || r.diasSemVenda() >= dias)
                .sorted((a, b) -> {
                    long diasA = a.diasSemVenda() == null ? Long.MAX_VALUE : a.diasSemVenda();
                    long diasB = b.diasSemVenda() == null ? Long.MAX_VALUE : b.diasSemVenda();
                    return Long.compare(diasB, diasA);
                })
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

    private VendaDetalheResponse toVendaDetalhe(Venda v) {
        List<ItemVendaResumo> itens = v.getItens().stream()
                .map(i -> new ItemVendaResumo(i.getNomeProdutoSnapshot(), i.getTamanhoSnapshot(), i.getCorSnapshot(), i.getQuantidade()))
                .toList();
        List<PagamentoResumo> pagamentos = v.getPagamentos().stream()
                .map(p -> new PagamentoResumo(p.getForma().name(), p.getValor(), p.getParcelas(), p.getReferencia()))
                .toList();
        return new VendaDetalheResponse(
                v.getId(), v.getCreatedAt(),
                v.getCliente() != null ? v.getCliente().getNome() : "Consumidor final",
                v.getCliente() != null ? v.getCliente().getTelefone() : null,
                v.getUsuario().getNome(), v.getLoja().getNome(),
                v.getSubtotal(), v.getDescontoGeral(), v.getValorTotal(), v.getTroco(), v.getObservacoes(),
                itens, pagamentos);
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
