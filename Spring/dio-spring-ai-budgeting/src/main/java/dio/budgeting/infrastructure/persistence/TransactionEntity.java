package dio.budgeting.infrastructure.persistence;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected TransactionEntity() {
    }

    static TransactionEntity fromDomain(Transaction t) {
        TransactionEntity e = new TransactionEntity();
        e.id = t.id().value();
        e.description = t.description();
        e.amount = t.amount();
        e.type = t.type();
        e.category = t.category();
        e.date = t.date();
        return e;
    }

    Transaction toDomain() {
        return new Transaction(new TransactionId(id), description, amount, type, category, date);
    }
}
