package dio.budgeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateTransactionUseCaseTest {

    private CreateTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateTransactionUseCase(new InMemoryTransactionRepository());
    }

    private static CreateTransactionCommand cmd(String description, String amount, TransactionType type,
                                                String category, LocalDate date) {
        return new CreateTransactionCommand(description, amount == null ? null : new BigDecimal(amount),
                type, category, date);
    }

    @Test
    void salvaTransacaoValidaNormalizandoCategoria() {
        Transaction t = useCase.execute(cmd("  Almoço  ", "45.9", TransactionType.EXPENSE, "  Alimentação ",
                LocalDate.now()));

        assertThat(t.id()).isNotNull();
        assertThat(t.description()).isEqualTo("Almoço");
        assertThat(t.amount()).isEqualByComparingTo("45.90");
        assertThat(t.category()).isEqualTo("alimentação");
    }

    @Test
    void usaCategoriaOutrosEDataDeHojeQuandoNaoInformadas() {
        Transaction t = useCase.execute(cmd("Café", "8", TransactionType.EXPENSE, null, null));

        assertThat(t.category()).isEqualTo("outros");
        assertThat(t.date()).isEqualTo(LocalDate.now());
    }

    @Test
    void rejeitaValorZeroOuNegativo() {
        assertThatThrownBy(() -> useCase.execute(cmd("Teste", "0", TransactionType.EXPENSE, null, null)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
        assertThatThrownBy(() -> useCase.execute(cmd("Teste", "-5", TransactionType.EXPENSE, null, null)))
                .isInstanceOf(InvalidTransactionException.class);
    }

    @Test
    void rejeitaValorComMaisDeDuasCasasDecimais() {
        assertThatThrownBy(() -> useCase.execute(cmd("Teste", "10.123", TransactionType.EXPENSE, null, null)))
                .hasMessageContaining("2 casas decimais");
    }

    @Test
    void rejeitaDescricaoEmBranco() {
        assertThatThrownBy(() -> useCase.execute(cmd("   ", "10", TransactionType.EXPENSE, null, null)))
                .hasMessageContaining("descrição");
    }

    @Test
    void rejeitaDataNoFuturo() {
        assertThatThrownBy(() -> useCase.execute(
                cmd("Teste", "10", TransactionType.EXPENSE, null, LocalDate.now().plusDays(1))))
                .hasMessageContaining("futuro");
    }

    @Test
    void rejeitaTipoAusente() {
        assertThatThrownBy(() -> useCase.execute(cmd("Teste", "10", null, null, null)))
                .hasMessageContaining("tipo");
    }

    @Test
    void acumulaTodasAsViolacoesDeUmaVez() {
        assertThatThrownBy(() -> useCase.execute(cmd("", "-1", null, null, null)))
                .isInstanceOfSatisfying(InvalidTransactionException.class,
                        ex -> assertThat(ex.violations()).hasSize(3));
    }
}
