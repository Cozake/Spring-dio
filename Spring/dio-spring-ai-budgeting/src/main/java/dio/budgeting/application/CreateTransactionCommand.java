package dio.budgeting.application;

import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionCommand(
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate date) {
}
