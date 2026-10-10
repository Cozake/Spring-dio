package dio.budgeting.infrastructure.ai;

/** O provedor de IA (OpenAI) falhou: chave invalida, sem rede, limite excedido etc. */
public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
