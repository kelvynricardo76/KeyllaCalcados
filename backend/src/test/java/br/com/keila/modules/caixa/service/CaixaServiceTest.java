package br.com.keila.modules.caixa.service;

import br.com.keila.modules.caixa.dto.FecharSessaoRequest;
import br.com.keila.modules.caixa.dto.SessaoResponse;
import br.com.keila.modules.caixa.model.Caixa;
import br.com.keila.modules.caixa.model.MovimentacaoCaixa;
import br.com.keila.modules.caixa.model.SessaoCaixa;
import br.com.keila.modules.caixa.model.StatusSessao;
import br.com.keila.modules.caixa.model.TipoMovCaixa;
import br.com.keila.modules.caixa.repository.CaixaRepository;
import br.com.keila.modules.caixa.repository.MovimentacaoCaixaRepository;
import br.com.keila.modules.caixa.repository.SessaoCaixaRepository;
import br.com.keila.modules.loja.model.Loja;
import br.com.keila.modules.usuario.model.PerfilUsuario;
import br.com.keila.modules.usuario.model.Usuario;
import br.com.keila.modules.venda.repository.PagamentoVendaRepository;
import br.com.keila.modules.venda.repository.VendaRepository;
import br.com.keila.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaixaServiceTest {

    @Mock private CaixaRepository caixaRepository;
    @Mock private SessaoCaixaRepository sessaoRepository;
    @Mock private MovimentacaoCaixaRepository movimentacaoRepository;
    @Mock private VendaRepository vendaRepository;
    @Mock private PagamentoVendaRepository pagamentoVendaRepository;

    private CaixaService caixaService;

    @BeforeEach
    void setUp() {
        caixaService = new CaixaService(caixaRepository, sessaoRepository, movimentacaoRepository, vendaRepository, pagamentoVendaRepository);
    }

    @Test
    void fecharSessao_calculaValorEsperadoEDiferencaCorretamente() {
        Caixa caixa = Caixa.builder().id(1L).loja(Loja.builder().id(1L).nome("Loja Centro").build()).nome("Caixa 1").ativo(true).build();
        Usuario usuario = Usuario.builder().id(1L).nome("Caixa 1").email("c@keila.com.br").perfil(PerfilUsuario.CAIXA).ativo(true).build();
        SessaoCaixa sessao = SessaoCaixa.builder()
                .id(9L).caixa(caixa).usuario(usuario).valorAbertura(new BigDecimal("100.00")).status(StatusSessao.ABERTA)
                .build();

        when(sessaoRepository.findById(9L)).thenReturn(Optional.of(sessao));
        when(sessaoRepository.save(any(SessaoCaixa.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimentacaoCaixa suprimento = MovimentacaoCaixa.builder().tipo(TipoMovCaixa.SUPRIMENTO).valor(new BigDecimal("50.00")).usuario(usuario).build();
        MovimentacaoCaixa sangria = MovimentacaoCaixa.builder().tipo(TipoMovCaixa.SANGRIA).valor(new BigDecimal("30.00")).usuario(usuario).build();
        MovimentacaoCaixa despesa = MovimentacaoCaixa.builder().tipo(TipoMovCaixa.DESPESA).valor(new BigDecimal("10.00")).usuario(usuario).build();
        when(movimentacaoRepository.findBySessaoIdOrderByCreatedAtDesc(9L))
                .thenReturn(List.of(suprimento, sangria, despesa));

        when(pagamentoVendaRepository.somarPagamentosDinheiroPorSessao(9L)).thenReturn(new BigDecimal("200.00"));
        when(vendaRepository.somarTrocoPorSessao(9L)).thenReturn(new BigDecimal("15.00"));

        SessaoResponse resposta = caixaService.fecharSessao(9L, new FecharSessaoRequest(new BigDecimal("290.00"), "Conferido"));

        // esperado = 100 (abertura) + 50 (suprimento) + 200 (vendas dinheiro) - 30 (sangria) - 10 (despesa) - 15 (troco) = 295
        assertThat(resposta.valorFechamentoCalculado()).isEqualByComparingTo("295.00");
        assertThat(resposta.diferenca()).isEqualByComparingTo("5.00");
        assertThat(resposta.status()).isEqualTo(StatusSessao.FECHADA);
    }

    @Test
    void fecharSessao_jaFechada_lancaRegraNegocio() {
        SessaoCaixa sessao = SessaoCaixa.builder().id(9L).status(StatusSessao.FECHADA).build();
        when(sessaoRepository.findById(9L)).thenReturn(Optional.of(sessao));

        assertThatThrownBy(() -> caixaService.fecharSessao(9L, new FecharSessaoRequest(BigDecimal.ZERO, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já está fechada");
    }
}
