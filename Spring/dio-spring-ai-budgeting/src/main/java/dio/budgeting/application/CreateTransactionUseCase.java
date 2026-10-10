package dio.budgeting.application;

import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Cria uma transacao.
 *
 * MELHORIA DO DESAFIO: todas as validacoes acontecem aqui, antes de salvar, e valem tanto para
 * o endpoint REST quanto para o Tool Calling da IA (o modelo recebe a mensagem de erro e explica ao usuario).
 */
@Component
public class CreateTransactionUseCase {

    static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");
    static final int MAX_DESCRIPTION = 120;
    static final int MAX_CATEGORY = 60;
    static final String DEFAULT_CATEGORY = "outros";

    private final TransactionRepository repository;

    public CreateTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction execute(CreateTransactionCommand command) {
        List<String> violations = new ArrayList<>();

        String description = command.description() == null ? "" : command.description().trim();
        if (description.isEmpty()) {
            violations.add("A descrição é obrigatória.");
        } else if (description.length() > MAX_DESCRIPTION) {
            violations.add("A descrição deve ter no máximo " + MAX_DESCRIPTION + " caracteres.");
        }

        BigDecimal amount = command.amount();
        if (amount == null || amount.signum() <= 0) {
            violations.add("O valor deve ser maior que zero.");
        } else {
            if (amount.stripTrailingZeros().scale() > 2) {
                violations.add("O valor deve ter no máximo 2 casas decimais.");
            }
            if (amount.compareTo(MAX_AMOUNT) > 0) {
                violations.add("O valor máximo por transação é R$ 10.000.000,00.");
            }
        }

        if (command.type() == null) {
            violations.add("O tipo é obrigatório (INCOME para receita ou EXPENSE para despesa).");
        }

        LocalDate today = LocalDate.now();
        LocalDate date = command.date() == null ? today : command.date();
        if (date.isAfter(today)) {
            violations.add("A data não pode estar no futuro.");
        }

        String category = normalizeCategory(command.category());
        if (category.length() > MAX_CATEGORY) {
            violations.add("A categoria deve ter no máximo " + MAX_CATEGORY + " caracteres.");
        }

        if (!violations.isEmpty()) {
            throw new InvalidTransactionException(violations);
        }

        Transaction transaction = new Transaction(
                TransactionId.generate(),
                description,
                amount.setScale(2, java.math.RoundingMode.UNNECESSARY),
                command.type(),
                category,
                date);

        return repository.save(transaction);
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return DEFAULT_CATEGORY;
        }
        return category.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
    }
}
