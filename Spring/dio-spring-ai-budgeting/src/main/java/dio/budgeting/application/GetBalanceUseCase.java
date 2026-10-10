package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/** Calcula receitas, despesas e saldo em um periodo (datas opcionais). */
@Component
public class GetBalanceUseCase {

    private final TransactionRepository repository;

    public GetBalanceUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public BalanceSummary execute(LocalDate start, LocalDate end) {
        List<Transaction> transactions = repository.findAll(TransactionFilter.ofPeriod(start, end));

        BigDecimal income = sum(transactions, TransactionType.INCOME);
        BigDecimal expense = sum(transactions, TransactionType.EXPENSE);

        return new BalanceSummary(income, expense, income.subtract(expense), transactions.size());
    }

    private static BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.type() == type)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
