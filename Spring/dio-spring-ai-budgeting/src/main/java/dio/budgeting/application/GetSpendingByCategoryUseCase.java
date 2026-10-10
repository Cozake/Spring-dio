package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * NOVA CONSULTA (melhoria do desafio): agrupa as despesas por categoria,
 * da que mais pesa para a que menos pesa, com o percentual de cada uma.
 */
@Component
public class GetSpendingByCategoryUseCase {

    private final TransactionRepository repository;

    public GetSpendingByCategoryUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<CategorySpending> execute(LocalDate start, LocalDate end) {
        List<Transaction> expenses = repository
                .findAll(new TransactionFilter(start, end, TransactionType.EXPENSE, null));

        BigDecimal grandTotal = expenses.stream()
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (grandTotal.signum() == 0) {
            return List.of();
        }

        Map<String, BigDecimal> totalsByCategory = expenses.stream()
                .collect(Collectors.groupingBy(Transaction::category,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));

        return totalsByCategory.entrySet().stream()
                .map(e -> new CategorySpending(
                        e.getKey(),
                        e.getValue().setScale(2, RoundingMode.HALF_UP),
                        e.getValue().multiply(BigDecimal.valueOf(100))
                                .divide(grandTotal, 1, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(CategorySpending::total).reversed())
                .toList();
    }
}
