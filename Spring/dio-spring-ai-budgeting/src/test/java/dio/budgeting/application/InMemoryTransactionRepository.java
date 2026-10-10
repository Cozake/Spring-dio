package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Repositorio falso para testar os casos de uso sem banco de dados. */
class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> data = new ArrayList<>();

    @Override
    public Transaction save(Transaction transaction) {
        data.add(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> findAll(TransactionFilter f) {
        return data.stream()
                .filter(t -> f.start() == null || !t.date().isBefore(f.start()))
                .filter(t -> f.end() == null || !t.date().isAfter(f.end()))
                .filter(t -> f.type() == null || t.type() == f.type())
                .filter(t -> f.category() == null || t.category().equalsIgnoreCase(f.category()))
                .sorted(Comparator.comparing(Transaction::date).reversed())
                .toList();
    }
}
