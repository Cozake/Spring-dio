package dio.budgeting.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dio.budgeting.infrastructure.ai.BudgetAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Sobe a aplicacao inteira (com H2) e testa os endpoints. A OpenAI e substituida por mocks. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BudgetAssistant assistant;

    @MockitoBean
    TranscriptionModel transcriptionModel;

    @MockitoBean
    TextToSpeechModel textToSpeechModel;

    @Test
    void criaTransacaoERetorna201() throws Exception {
        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Mercado","amount":120.50,"type":"EXPENSE","category":"Alimentação"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.category").value("alimentação"))
                .andExpect(jsonPath("$.amount").value(120.50));
    }

    @Test
    void valorInvalidoRetorna400ComListaDeViolacoes() throws Exception {
        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","amount":-5,"type":"EXPENSE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations.length()").value(2));
    }

    @Test
    void saldoEGastosPorCategoria() throws Exception {
        for (String body : new String[]{
                "{\"description\":\"Salário\",\"amount\":2000,\"type\":\"INCOME\",\"category\":\"salário\"}",
                "{\"description\":\"Mercado\",\"amount\":300,\"type\":\"EXPENSE\",\"category\":\"alimentação\"}",
                "{\"description\":\"Ônibus\",\"amount\":100,\"type\":\"EXPENSE\",\"category\":\"transporte\"}"}) {
            mvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated());
        }

        mvc.perform(get("/api/transactions/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1600.0));

        mvc.perform(get("/api/transactions/spending-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("alimentação"))
                .andExpect(jsonPath("$[0].percentage").value(75.0));

        mvc.perform(get("/api/transactions").param("type", "EXPENSE"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void dataEmFormatoInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/transactions/balance").param("start", "ontem"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void comandoDeTextoUsaOAssistente() throws Exception {
        when(assistant.answer("qual meu saldo?")).thenReturn("Seu saldo é zero.");

        mvc.perform(post("/api/ai/text-command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"qual meu saldo?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Seu saldo é zero."));
    }

    @Test
    void comandoDeVozDevolveAudioMp3() throws Exception {
        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("gastei vinte reais");
        when(assistant.answer("gastei vinte reais")).thenReturn("Registrado.");
        when(textToSpeechModel.call("Registrado.")).thenReturn(new byte[]{1, 2, 3});

        var audio = new MockMultipartFile("audio", "comando.mp3", "audio/mpeg", new byte[]{7, 7, 7});

        mvc.perform(multipart("/api/ai/voice-command").file(audio))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("X-Transcription", "gastei%20vinte%20reais"));
    }
}
