package dio.budgeting.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /** ChatClient com as ferramentas do orcamento registradas por padrao. */
    @Bean
    ChatClient budgetingChatClient(ChatClient.Builder builder, BudgetingTools tools) {
        return builder.defaultTools(tools).build();
    }
}
