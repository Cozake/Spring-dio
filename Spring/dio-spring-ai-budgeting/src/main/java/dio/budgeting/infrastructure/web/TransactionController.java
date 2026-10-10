package dio.budgeting.infrastructure.web;

import dio.budgeting.application.BalanceSummary;
import dio.budgeting.application.CategorySpending;
import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.GetBalanceUseCase;
import dio.budgeting.application.GetSpendingByCategoryUseCase;
import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransaction;
    private final ListTransactionsUseCase listTransactions;
    private final GetBalanceUseCase getBalance;
    private final GetSpendingByCategoryUseCase getSpendingByCategory;

    public TransactionController(CreateTransactionUseCase createTransaction,
                                 ListTransactionsUseCase listTransactions,
                                 GetBalanceUseCase getBalance,
                                 GetSpendingByCategoryUseCase getSpendingByCategory) {
        this.createTransaction = createTransaction;
        this.listTransactions = listTransactions;
        this.getBalance = getBalance;
        this.getSpendingByCategory = getSpendingByCategory;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody CreateTransactionRequest request) {
        var created = createTransaction.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(created));
    }

    @GetMapping
    public List<TransactionResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category) {
        return listTransactions.execute(new TransactionFilter(start, end, type, category))
                .stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/balance")
    public BalanceSummary balance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return getBalance.execute(start, end);
    }

    @GetMapping("/spending-by-category")
    public List<CategorySpending> spendingByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return getSpendingByCategory.execute(start, end);
    }
}
