package dio.budgeting.infrastructure.web;

import dio.budgeting.infrastructure.ai.VoiceAssistantService;
import dio.budgeting.infrastructure.ai.VoiceResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final VoiceAssistantService voiceAssistant;

    public AiController(VoiceAssistantService voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    /** Recebe um audio, executa a acao financeira pedida e devolve a resposta falada (MP3). */
    @PostMapping(value = "/voice-command", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> voiceCommand(@RequestParam("audio") MultipartFile audio) throws IOException {
        VoiceResult result = voiceAssistant.processVoice(audio.getOriginalFilename(), audio.getBytes());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                // Headers HTTP nao aceitam acentos, entao o texto vai URL-encoded.
                .header("X-Transcription", encode(result.transcription()))
                .header("X-Assistant-Text", encode(result.answerText()))
                .body(result.answerAudio());
    }

    /** Mesmo fluxo, mas so com texto (sem Whisper e sem TTS): bom para testar rapido. */
    @PostMapping("/text-command")
    public TextCommandResponse textCommand(@RequestBody TextCommandRequest request) {
        return new TextCommandResponse(voiceAssistant.processText(request.text()));
    }

    public record TextCommandRequest(String text) {
    }

    public record TextCommandResponse(String answer) {
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
