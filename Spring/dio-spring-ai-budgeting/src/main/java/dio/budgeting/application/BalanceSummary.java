package dio.budgeting.application;

import java.math.BigDecimal;

public record BalanceSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance, int transactionCount) {
}
