package dio.budgeting.infrastructure.ai;

import dio.budgeting.domain.Transaction;
import java.math.BigDecimal;

/** Representacao simples (so tipos primitivos/strings) para o modelo de linguagem ler. */
public record TransactionView(String id, String description, BigDecimal amount, String type, String category,
                              String date) {

    static TransactionView from(Transaction t) {
        return new TransactionView(t.id().value().toString(), t.description(), t.amount(), t.type().name(),
                t.category(), t.date().toString());
    }
}
