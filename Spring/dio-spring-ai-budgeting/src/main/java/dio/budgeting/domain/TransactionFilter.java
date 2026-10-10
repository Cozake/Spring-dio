package dio.budgeting.domain;

import java.time.LocalDate;

/** Filtros opcionais para consultar transacoes. Campos nulos significam "sem filtro". */
public record TransactionFilter(LocalDate start, LocalDate end, TransactionType type, String category) {

    public TransactionFilter {
        if (start != null && end != null && start.isAfter(end)) {
            throw new InvalidTransactionException("A data inicial não pode ser posterior à data final.");
        }
    }

    public static TransactionFilter ofPeriod(LocalDate start, LocalDate end) {
        return new TransactionFilter(start, end, null, null);
    }
}
