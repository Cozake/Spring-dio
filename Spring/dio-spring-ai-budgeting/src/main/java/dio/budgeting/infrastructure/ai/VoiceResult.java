package dio.budgeting.infrastructure.ai;

public record VoiceResult(String transcription, String answerText, byte[] answerAudio) {
}
