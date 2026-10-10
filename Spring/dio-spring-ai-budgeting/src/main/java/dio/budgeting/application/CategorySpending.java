package dio.budgeting.application;

import java.math.BigDecimal;

public record CategorySpending(String category, BigDecimal total, BigDecimal percentage) {
}
