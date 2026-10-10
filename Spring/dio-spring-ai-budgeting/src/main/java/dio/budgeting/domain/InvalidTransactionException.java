package dio.budgeting.domain;

import java.util.List;

/** Lancada quando os dados de uma transacao (ou de uma consulta) violam regras do dominio. */
public class InvalidTransactionException extends RuntimeException {

    private final List<String> violations;

    public InvalidTransactionException(List<String> violations) {
        super(String.join(" ", violations));
        this.violations = List.copyOf(violations);
    }

    public InvalidTransactionException(String violation) {
        this(List.of(violation));
    }

    public List<String> violations() {
        return violations;
    }
}
