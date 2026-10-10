package dio.budgeting.infrastructure.ai;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Orquestra o fluxo completo:
 * audio -> texto (speech-to-text) -> IA + ferramentas -> texto final -> audio (text-to-speech).
 */
@Service
public class VoiceAssistantService {

    static final String NOT_UNDERSTOOD = "Desculpe, não consegui entender o áudio. Pode repetir, por favor?";

    private final TranscriptionModel transcriptionModel;
    private final BudgetAssistant assistant;
    private final TextToSpeechModel textToSpeechModel;

    public VoiceAssistantService(TranscriptionModel transcriptionModel,
                                 BudgetAssistant assistant,
                                 TextToSpeechModel textToSpeechModel) {
        this.transcriptionModel = transcriptionModel;
        this.assistant = assistant;
        this.textToSpeechModel = textToSpeechModel;
    }

    public VoiceResult processVoice(String filename, byte[] audio) {
        if (audio == null || audio.length == 0) {
            throw new IllegalArgumentException("O arquivo de áudio está vazio.");
        }
        String safeName = (filename == null || filename.isBlank()) ? "audio.mp3" : filename;

        try {
            String transcription = transcriptionModel.transcribe(namedResource(audio, safeName));

            String answer = (transcription == null || transcription.isBlank())
                    ? NOT_UNDERSTOOD
                    : assistant.answer(transcription);

            byte[] speech = textToSpeechModel.call(answer);
            return new VoiceResult(transcription == null ? "" : transcription, answer, speech);
        } catch (RuntimeException ex) {
            throw new AssistantUnavailableException(
                    "Falha ao processar o comando de voz com o provedor de IA: " + ex.getMessage(), ex);
        }
    }

    /** Versao so texto: util para testar sem gastar com audio. */
    public String processText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("O texto do comando é obrigatório.");
        }
        try {
            return assistant.answer(text);
        } catch (RuntimeException ex) {
            throw new AssistantUnavailableException(
                    "Falha ao processar o comando com o provedor de IA: " + ex.getMessage(), ex);
        }
    }

    // O Whisper identifica o formato pela extensao do nome do arquivo, por isso sobrescrevemos getFilename().
    private static Resource namedResource(byte[] audio, String filename) {
        return new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
