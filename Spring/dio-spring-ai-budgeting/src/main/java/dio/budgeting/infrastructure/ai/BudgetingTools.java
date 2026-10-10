package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.CreateTransactionCommand;
import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.GetBalanceUseCase;
import dio.budgeting.application.GetSpendingByCategoryUseCase;
import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Ferramentas (Tool Calling) que o modelo pode chamar. Note que elas NAO tem regra de negocio:
 * apenas traduzem os parametros do modelo e delegam para os mesmos casos de uso usados pelo REST.
 */
@Component
public class BudgetingTools {

    private static final int MAX_LISTED = 30;

    private final CreateTransactionUseCase createTransaction;
    private final ListTransactionsUseCase listTransactions;
    private final GetBalanceUseCase getBalance;
    private final GetSpendingByCategoryUseCase getSpendingByCategory;

    public BudgetingTools(CreateTransactionUseCase createTransaction,
                          ListTransactionsUseCase listTransactions,
                          GetBalanceUseCase getBalance,
                          GetSpendingByCategoryUseCase getSpendingByCategory) {
        this.createTransaction = createTransaction;
        this.listTransactions = listTransactions;
        this.getBalance = getBalance;
        this.getSpendingByCategory = getSpendingByCategory;
    }

    @Tool(description = "Registra uma nova transação financeira (receita ou despesa). Use quando o usuário "
            + "disser que gastou, pagou, comprou, recebeu ou ganhou algum valor.")
    public ToolResponse createTransaction(
            @ToolParam(description = "Descrição curta, ex.: 'Almoço no restaurante'") String description,
            @ToolParam(description = "Valor positivo em reais, ex.: 45.90") BigDecimal amount,
            @ToolParam(description = "INCOME para receitas/entradas ou EXPENSE para despesas/saídas") String type,
            @ToolParam(description = "Categoria, ex.: alimentação, transporte, lazer, salário", required = false) String category,
            @ToolParam(description = "Data no formato yyyy-MM-dd. Se omitida, usa hoje", required = false) String date) {
        return safely(() -> {
            var created = createTransaction.execute(new CreateTransactionCommand(
                    description, amount, parseType(type), category, parseDate(date)));
            return ToolResponse.ok("Transação registrada com sucesso.", TransactionView.from(created));
        });
    }

    @Tool(description = "Lista transações (mais recentes primeiro) com filtros opcionais de período, tipo e categoria. "
            + "Use para perguntas como 'quais foram meus gastos de ontem?'.")
    public ToolResponse listTransactions(
            @ToolParam(description = "Data inicial yyyy-MM-dd", required = false) String startDate,
            @ToolParam(description = "Data final yyyy-MM-dd", required = false) String endDate,
            @ToolParam(description = "INCOME ou EXPENSE", required = false) String type,
            @ToolParam(description = "Categoria", required = false) String category) {
        return safely(() -> {
            var all = listTransactions.execute(new TransactionFilter(
                    parseDate(startDate), parseDate(endDate), parseTypeOrNull(type), category));
            List<TransactionView> views = all.stream().limit(MAX_LISTED).map(TransactionView::from).toList();
            String message = all.size() + " transação(ões) encontrada(s)"
                    + (all.size() > MAX_LISTED ? " (mostrando as " + MAX_LISTED + " mais recentes)." : ".");
            return ToolResponse.ok(message, views);
        });
    }

    @Tool(description = "Calcula o total de receitas, o total de despesas e o saldo em um período. "
            + "Use para 'quanto sobrou este mês?' ou 'qual meu saldo?'.")
    public ToolResponse getBalance(
            @ToolParam(description = "Data inicial yyyy-MM-dd", required = false) String startDate,
            @ToolParam(description = "Data final yyyy-MM-dd", required = false) String endDate) {
        return safely(() -> ToolResponse.ok("Saldo calculado.",
                getBalance.execute(parseDate(startDate), parseDate(endDate))));
    }

    @Tool(description = "Mostra as despesas agrupadas por categoria, com total e percentual de cada uma. "
            + "Use para 'com o que eu mais gastei?' ou 'quanto gastei em cada categoria?'.")
    public ToolResponse getSpendingByCategory(
            @ToolParam(description = "Data inicial yyyy-MM-dd", required = false) String startDate,
            @ToolParam(description = "Data final yyyy-MM-dd", required = false) String endDate) {
        return safely(() -> ToolResponse.ok("Despesas por categoria calculadas.",
                getSpendingByCategory.execute(parseDate(startDate), parseDate(endDate))));
    }

    // ---------- helpers ----------

    private static ToolResponse safely(Supplier<ToolResponse> action) {
        try {
            return action.get();
        } catch (InvalidTransactionException ex) {
            return ToolResponse.error(ex.getMessage());
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidTransactionException("Data inválida: '" + value + "'. Use o formato yyyy-MM-dd.");
        }
    }

    private static TransactionType parseType(String value) {
        TransactionType type = parseTypeOrNull(value);
        if (type == null) {
            throw new InvalidTransactionException("O tipo é obrigatório (INCOME ou EXPENSE).");
        }
        return type;
    }

    private static TransactionType parseTypeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransactionException("Tipo inválido: '" + value + "'. Use INCOME ou EXPENSE.");
        }
    }
}
