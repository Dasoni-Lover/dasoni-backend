package dasoni_backend.global.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TtsTestController {

    private final TextToSpeechModel tts;

    @GetMapping("/tts/test")
    public ResponseEntity<byte[]> ttsTest(
            @RequestParam(defaultValue = "해원이의 다소니 프로젝트 화이팅!") String text,
            @RequestParam String voiceId   // IVC로 만든 voice_id 넣어서 호출
    ) {
        var options = ElevenLabsTextToSpeechOptions.builder()
                .modelId("eleven_turbo_v2_5")
                .voiceId(voiceId)
                .outputFormat("mp3_44100_128")
                .build();

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
        TextToSpeechResponse res = tts.call(prompt);

        byte[] audio = res.getResult().getOutput();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tts.mp3")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(audio);
    }
}
