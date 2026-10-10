package dio.budgeting.infrastructure.persistence;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa o contrato do dominio usando Spring Data JPA. */
@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final SpringDataTransactionRepository jpa;

    public JpaTransactionRepository(SpringDataTransactionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return jpa.save(TransactionEntity.fromDomain(transaction)).toDomain();
    }

    @Override
    public List<Transaction> findAll(TransactionFilter filter) {
        Specification<TransactionEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.start() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("date"), filter.start()));
            }
            if (filter.end() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("date"), filter.end()));
            }
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.category() != null) {
                predicates.add(cb.equal(cb.lower(root.<String>get("category")), filter.category().toLowerCase()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.desc("createdAt"));
        return jpa.findAll(spec, sort).stream().map(TransactionEntity::toDomain).toList();
    }
}
