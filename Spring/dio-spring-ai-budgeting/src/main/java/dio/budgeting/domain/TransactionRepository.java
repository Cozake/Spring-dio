package dio.budgeting.domain;

import java.util.List;

/** Contrato de persistencia. A implementacao fica na camada de infraestrutura. */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    /** Retorna as transacoes que atendem ao filtro, da mais recente para a mais antiga. */
    List<Transaction> findAll(TransactionFilter filter);
}
