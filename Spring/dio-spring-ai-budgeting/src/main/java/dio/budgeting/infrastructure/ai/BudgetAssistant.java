package dio.budgeting.infrastructure.ai;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/** Entende um comando em texto, chama as ferramentas necessarias e devolve a resposta final. */
@Component
public class BudgetAssistant {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente financeiro por voz de uma API de orçamento.
            Responda sempre em português do Brasil, de forma curta e natural, porque a resposta será
            lida em voz alta: não use markdown, listas, emojis nem símbolos.
            Use SEMPRE as ferramentas para registrar ou consultar dados; nunca invente valores ou transações.
            Hoje é %s (%s). Converta datas relativas como 'ontem', 'este mês' ou 'semana passada'
            para o formato yyyy-MM-dd antes de chamar as ferramentas.
            Se faltar o valor ou o tipo (receita ou despesa), pergunte ao usuário em vez de assumir.
            Se uma ferramenta retornar erro, explique o motivo ao usuário de forma simples.
            Os valores são em reais (R$).
            """;

    private final ChatClient chatClient;

    public BudgetAssistant(ChatClient budgetingChatClient) {
        this.chatClient = budgetingChatClient;
    }

    public String answer(String userText) {
        LocalDate today = LocalDate.now();
        String weekday = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));

        return chatClient.prompt()
                .system(SYSTEM_PROMPT.formatted(today, weekday))
                .user(userText)
                .call()
                .content();
    }
}
