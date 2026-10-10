package dio.budgeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryUseCasesTest {

    private final LocalDate today = LocalDate.now();
    private CreateTransactionUseCase create;
    private GetBalanceUseCase balance;
    private GetSpendingByCategoryUseCase spending;
    private ListTransactionsUseCase list;

    @BeforeEach
    void setUp() {
        var repo = new InMemoryTransactionRepository();
        create = new CreateTransactionUseCase(repo);
        balance = new GetBalanceUseCase(repo);
        spending = new GetSpendingByCategoryUseCase(repo);
        list = new ListTransactionsUseCase(repo);

        add("Salário", "3000", TransactionType.INCOME, "salário", today);
        add("Mercado", "300", TransactionType.EXPENSE, "alimentação", today);
        add("Restaurante", "100", TransactionType.EXPENSE, "alimentação", today.minusDays(1));
        add("Uber", "100", TransactionType.EXPENSE, "transporte", today.minusDays(40));
    }

    private void add(String d, String amount, TransactionType type, String category, LocalDate date) {
        create.execute(new CreateTransactionCommand(d, new BigDecimal(amount), type, category, date));
    }

    @Test
    void calculaSaldoDeTodoOPeriodo() {
        BalanceSummary b = balance.execute(null, null);

        assertThat(b.totalIncome()).isEqualByComparingTo("3000");
        assertThat(b.totalExpense()).isEqualByComparingTo("500");
        assertThat(b.balance()).isEqualByComparingTo("2500");
        assertThat(b.transactionCount()).isEqualTo(4);
    }

    @Test
    void calculaSaldoSomenteDentroDoPeriodo() {
        BalanceSummary b = balance.execute(today.minusDays(7), today);

        assertThat(b.totalExpense()).isEqualByComparingTo("400");
        assertThat(b.balance()).isEqualByComparingTo("2600");
    }

    @Test
    void agrupaDespesasPorCategoriaEmOrdemDecrescente() {
        List<CategorySpending> result = spending.execute(null, null);

        assertThat(result).extracting(CategorySpending::category).containsExactly("alimentação", "transporte");
        assertThat(result.get(0).total()).isEqualByComparingTo("400");
        assertThat(result.get(0).percentage()).isEqualByComparingTo("80.0");
        assertThat(result.get(1).percentage()).isEqualByComparingTo("20.0");
    }

    @Test
    void semDespesasRetornaListaVazia() {
        assertThat(spending.execute(today.plusDays(10), today.plusDays(20))).isEmpty();
    }

    @Test
    void listaFiltrandoPorTipoECategoriaIgnorandoMaiusculas() {
        var result = list.execute(new TransactionFilter(null, null, TransactionType.EXPENSE, "  ALIMENTAÇÃO "));

        assertThat(result).hasSize(2);
    }

    @Test
    void rejeitaPeriodoComInicioDepoisDoFim() {
        assertThatThrownBy(() -> balance.execute(today, today.minusDays(1)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("data inicial");
    }
}
