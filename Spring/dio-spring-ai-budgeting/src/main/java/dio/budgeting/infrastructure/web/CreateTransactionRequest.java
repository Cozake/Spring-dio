package dio.budgeting.infrastructure.web;

import dio.budgeting.application.CreateTransactionCommand;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate date) {

    CreateTransactionCommand toCommand() {
        return new CreateTransactionCommand(description, amount, type, category, date);
    }
}
