package dio.budgeting.infrastructure.web;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate date) {

    static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.id().value(), t.description(), t.amount(), t.type(), t.category(), t.date());
    }
}
