package dio.budgeting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dio.budgeting.infrastructure.ai.AssistantUnavailableException;
import dio.budgeting.infrastructure.ai.BudgetAssistant;
import dio.budgeting.infrastructure.ai.VoiceAssistantService;
import dio.budgeting.infrastructure.ai.VoiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.Resource;

/** Testa a orquestracao audio -> texto -> IA -> audio com todos os modelos "mockados". */
class VoiceAssistantServiceTest {

    private TranscriptionModel transcription;
    private BudgetAssistant assistant;
    private TextToSpeechModel tts;
    private VoiceAssistantService service;

    @BeforeEach
    void setUp() {
        transcription = mock(TranscriptionModel.class);
        assistant = mock(BudgetAssistant.class);
        tts = mock(TextToSpeechModel.class);
        service = new VoiceAssistantService(transcription, assistant, tts);
    }

    @Test
    void executaFluxoCompleto() {
        byte[] speech = {1, 2, 3};
        when(transcription.transcribe(any(Resource.class))).thenReturn("gastei 20 reais no mercado");
        when(assistant.answer("gastei 20 reais no mercado")).thenReturn("Despesa registrada.");
        when(tts.call("Despesa registrada.")).thenReturn(speech);

        VoiceResult result = service.processVoice("comando.mp3", new byte[]{9, 9});

        assertThat(result.transcription()).isEqualTo("gastei 20 reais no mercado");
        assertThat(result.answerText()).isEqualTo("Despesa registrada.");
        assertThat(result.answerAudio()).containsExactly(1, 2, 3);
    }

    @Test
    void naoChamaAIAQuandoATranscricaoVeioVazia() {
        when(transcription.transcribe(any(Resource.class))).thenReturn("  ");
        when(tts.call(anyString())).thenReturn(new byte[]{1});

        VoiceResult result = service.processVoice("comando.mp3", new byte[]{9});

        assertThat(result.answerText()).contains("não consegui entender");
        verifyNoInteractions(assistant);
        verify(tts).call(result.answerText());
    }

    @Test
    void rejeitaAudioVazio() {
        assertThatThrownBy(() -> service.processVoice("a.mp3", new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void converteFalhaDoProvedorEmAssistantUnavailable() {
        when(transcription.transcribe(any(Resource.class))).thenThrow(new RuntimeException("401"));

        assertThatThrownBy(() -> service.processVoice("a.mp3", new byte[]{1}))
                .isInstanceOf(AssistantUnavailableException.class);
    }
}
