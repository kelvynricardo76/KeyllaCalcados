package br.com.keila.modules.venda.service;

import br.com.keila.modules.caixa.repository.SessaoCaixaRepository;
import br.com.keila.modules.cliente.model.Cliente;
import br.com.keila.modules.cliente.repository.ClienteRepository;
import br.com.keila.modules.estoque.service.EstoqueService;
import br.com.keila.modules.fiado.service.FiadoService;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.loja.repository.LojaRepository;
import br.com.keila.modules.produto.model.Produto;
import br.com.keila.modules.produto.model.VariacaoProduto;
import br.com.keila.modules.produto.repository.VariacaoProdutoRepository;
import br.com.keila.modules.usuario.model.PerfilUsuario;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.venda.dto.AdicionarItemRequest;
import br.com.keila.modules.venda.dto.FinalizarVendaRequest;
import br.com.keila.modules.venda.dto.PagamentoInput;
import br.com.keila.modules.venda.dto.VendaResponse;
import br.com.keila.modules.venda.model.FormaPagamento;
import br.com.keila.modules.venda.model.ItemVenda;
import br.com.keila.modules.venda.model.StatusVenda;
import br.com.keila.modules.venda.model.Venda;
import br.com.keila.modules.venda.repository.VendaRepository;
import br.com.keila.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock private VendaRepository vendaRepository;
    @Mock private SessaoCaixaRepository sessaoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private LojaRepository lojaRepository;
    @Mock private VariacaoProdutoRepository variacaoRepository;
    @Mock private EstoqueService estoqueService;
    @Mock private FiadoService fiadoService;

    private VendaService vendaService;

    private Loja loja;
    private VariacaoProduto variacao;

    @BeforeEach
    void setUp() {
        vendaService = new VendaService(vendaRepository, sessaoRepository, clienteRepository, lojaRepository,
                variacaoRepository, estoqueService, fiadoService);

        loja = Loja.builder().id(1L).nome("Loja Centro").build();
        Produto produto = Produto.builder().id(1L).nome("Tênis Esportivo").precoVenda(new BigDecimal("100.00")).build();
        variacao = VariacaoProduto.builder().id(10L).produto(produto).sku("TEN-38-PRETO").ativo(true).build();

        Usuario usuario = Usuario.builder().id(1L).nome("Caixa 1").email("caixa@keila.com.br").perfil(PerfilUsuario.CAIXA).ativo(true).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null));

        lenient().when(vendaRepository.save(any(Venda.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Venda vendaAberta(Cliente cliente) {
        return Venda.builder()
                .id(100L).loja(loja).usuario(SecurityContextHolder.getContext().getAuthentication() != null
                        ? (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null)
                .cliente(cliente).status(StatusVenda.ABERTA)
                .subtotal(BigDecimal.ZERO).descontoGeral(BigDecimal.ZERO).valorTotal(BigDecimal.ZERO)
                .build();
    }

    @Test
    void adicionarItem_comEstoqueInsuficiente_lancaRegraNegocio() {
        Venda venda = vendaAberta(null);
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));
        when(variacaoRepository.findById(10L)).thenReturn(Optional.of(variacao));
        when(estoqueService.saldoDisponivel(10L, 1L)).thenReturn(2);

        assertThatThrownBy(() -> vendaService.adicionarItem(100L, new AdicionarItemRequest(10L, 5, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void adicionarItem_comEstoqueSuficiente_calculaSubtotalCorretamente() {
        Venda venda = vendaAberta(null);
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));
        when(variacaoRepository.findById(10L)).thenReturn(Optional.of(variacao));
        when(estoqueService.saldoDisponivel(10L, 1L)).thenReturn(10);

        VendaResponse resposta = vendaService.adicionarItem(100L, new AdicionarItemRequest(10L, 2, null));

        assertThat(resposta.subtotal()).isEqualByComparingTo("200.00");
        assertThat(resposta.itens()).hasSize(1);
    }

    @Test
    void finalizar_comSomaDePagamentosMenorQueOTotal_lancaRegraNegocio() {
        Venda venda = vendaAberta(null);
        venda.getItens().add(itemDe(venda, new BigDecimal("100.00")));
        venda.setSubtotal(new BigDecimal("100.00"));
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));

        FinalizarVendaRequest request = new FinalizarVendaRequest(
                BigDecimal.ZERO, List.of(new PagamentoInput(FormaPagamento.DINHEIRO, new BigDecimal("50.00"), 1, null)), null);

        assertThatThrownBy(() -> vendaService.finalizar(100L, request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("menor que o total");
    }

    @Test
    void finalizar_comTrocoSemDinheiroSuficiente_lancaRegraNegocio() {
        Venda venda = vendaAberta(null);
        venda.getItens().add(itemDe(venda, new BigDecimal("100.00")));
        venda.setSubtotal(new BigDecimal("100.00"));
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));

        FinalizarVendaRequest request = new FinalizarVendaRequest(
                BigDecimal.ZERO, List.of(new PagamentoInput(FormaPagamento.CREDITO, new BigDecimal("150.00"), 1, null)), null);

        assertThatThrownBy(() -> vendaService.finalizar(100L, request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("troco");
    }

    @Test
    void finalizar_comPagamentoFiadoSemCliente_lancaRegraNegocio() {
        Venda venda = vendaAberta(null);
        venda.getItens().add(itemDe(venda, new BigDecimal("100.00")));
        venda.setSubtotal(new BigDecimal("100.00"));
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));

        FinalizarVendaRequest request = new FinalizarVendaRequest(
                BigDecimal.ZERO, List.of(new PagamentoInput(FormaPagamento.FIADO, new BigDecimal("100.00"), 1, null)), 30);

        assertThatThrownBy(() -> vendaService.finalizar(100L, request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("cliente");
    }

    @Test
    void finalizar_comPagamentoFiadoComCliente_criaFiadoEBaixaEstoqueEFechaVenda() {
        Cliente cliente = Cliente.builder().id(5L).nome("Maria Silva").ativo(true).build();
        Venda venda = vendaAberta(cliente);
        ItemVenda item = itemDe(venda, new BigDecimal("100.00"));
        venda.getItens().add(item);
        venda.setSubtotal(new BigDecimal("100.00"));
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));

        FinalizarVendaRequest request = new FinalizarVendaRequest(
                BigDecimal.ZERO, List.of(new PagamentoInput(FormaPagamento.FIADO, new BigDecimal("100.00"), 1, null)), 15);

        VendaResponse resposta = vendaService.finalizar(100L, request);

        assertThat(resposta.status()).isEqualTo(StatusVenda.FECHADA);
        verify(fiadoService).criarParaVenda(eq(5L), eq(1L), eq(new BigDecimal("100.00")), any(), eq(100L));
        verify(estoqueService).baixarPorVenda(eq(10L), eq(1L), eq(item.getQuantidade()), eq(100L));
    }

    @Test
    void finalizar_semItens_lancaRegraNegocio() {
        Venda venda = vendaAberta(null);
        when(vendaRepository.findById(100L)).thenReturn(Optional.of(venda));

        FinalizarVendaRequest request = new FinalizarVendaRequest(
                BigDecimal.ZERO, List.of(new PagamentoInput(FormaPagamento.DINHEIRO, new BigDecimal("10.00"), 1, null)), null);

        assertThatThrownBy(() -> vendaService.finalizar(100L, request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ao menos um item");
    }

    private ItemVenda itemDe(Venda venda, BigDecimal subtotal) {
        return ItemVenda.builder()
                .id(1L).venda(venda).variacao(variacao).produto(variacao.getProduto())
                .nomeProdutoSnapshot(variacao.getProduto().getNome()).skuSnapshot(variacao.getSku())
                .quantidade(1).precoUnitario(subtotal).descontoItem(BigDecimal.ZERO).subtotal(subtotal)
                .build();
    }
}
