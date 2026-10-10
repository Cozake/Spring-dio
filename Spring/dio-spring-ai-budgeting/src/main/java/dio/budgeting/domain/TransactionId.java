package dio.budgeting.domain;

import java.util.UUID;

/** Identificador fortemente tipado, evita misturar UUIDs de entidades diferentes. */
public record TransactionId(UUID value) {

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }
}
