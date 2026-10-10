package dio.budgeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.GetBalanceUseCase;
import dio.budgeting.application.GetSpendingByCategoryUseCase;
import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.ai.BudgetingTools;
import dio.budgeting.infrastructure.ai.ToolResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testa as ferramentas que a IA chama, sem precisar da OpenAI. */
class BudgetingToolsTest {

    private BudgetingTools tools;

    @BeforeEach
    void setUp() {
        List<Transaction> data = new ArrayList<>();
        TransactionRepository repo = new TransactionRepository() {
            @Override
            public Transaction save(Transaction t) {
                data.add(t);
                return t;
            }

            @Override
            public List<Transaction> findAll(TransactionFilter f) {
                return data.stream()
                        .filter(t -> f.type() == null || t.type() == f.type())
                        .toList();
            }
        };
        tools = new BudgetingTools(new CreateTransactionUseCase(repo), new ListTransactionsUseCase(repo),
                new GetBalanceUseCase(repo), new GetSpendingByCategoryUseCase(repo));
    }

    @Test
    void registraDespesaComSucesso() {
        ToolResponse r = tools.createTransaction("Almoço", new BigDecimal("32.50"), "expense", "alimentação", null);

        assertThat(r.success()).isTrue();
        assertThat(r.data()).isNotNull();
    }

    @Test
    void devolveErroEmVezDeLancarExcecaoQuandoValorInvalido() {
        ToolResponse r = tools.createTransaction("Almoço", new BigDecimal("-1"), "EXPENSE", null, null);

        assertThat(r.success()).isFalse();
        assertThat(r.message()).contains("maior que zero");
    }

    @Test
    void devolveErroParaTipoInvalido() {
        ToolResponse r = tools.createTransaction("Almoço", BigDecimal.TEN, "gasto", null, null);

        assertThat(r.success()).isFalse();
        assertThat(r.message()).contains("INCOME ou EXPENSE");
    }

    @Test
    void devolveErroParaDataEmFormatoErrado() {
        ToolResponse r = tools.getBalance("ontem", null);

        assertThat(r.success()).isFalse();
        assertThat(r.message()).contains("yyyy-MM-dd");
    }

    @Test
    void consultaSaldoAposRegistrar() {
        tools.createTransaction("Salário", new BigDecimal("1000"), "INCOME", "salário", null);
        tools.createTransaction("Mercado", new BigDecimal("250"), "EXPENSE", "alimentação", null);

        ToolResponse r = tools.getBalance(null, null);

        assertThat(r.success()).isTrue();
        assertThat(r.data()).hasToString(
                "BalanceSummary[totalIncome=1000.00, totalExpense=250.00, balance=750.00, transactionCount=2]");
    }
}
