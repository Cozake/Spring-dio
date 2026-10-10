package dio.budgeting.infrastructure.ai;

/** Formato padrao devolvido pelas ferramentas ao modelo: sucesso/erro + mensagem + dados. */
public record ToolResponse(boolean success, String message, Object data) {

    public static ToolResponse ok(String message, Object data) {
        return new ToolResponse(true, message, data);
    }

    public static ToolResponse error(String message) {
        return new ToolResponse(false, message, null);
    }
}
