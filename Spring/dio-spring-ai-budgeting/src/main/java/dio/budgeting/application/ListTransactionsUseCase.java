package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ListTransactionsUseCase {

    private final TransactionRepository repository;

    public ListTransactionsUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> execute(TransactionFilter filter) {
        String category = filter.category() == null || filter.category().isBlank()
                ? null
                : filter.category().trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
        return repository.findAll(new TransactionFilter(filter.start(), filter.end(), filter.type(), category));
    }
}
