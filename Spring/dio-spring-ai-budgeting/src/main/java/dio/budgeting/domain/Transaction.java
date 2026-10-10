package dio.budgeting.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        TransactionId id,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate date) {
}
