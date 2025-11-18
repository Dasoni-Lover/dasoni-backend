package dasoni_backend.global.ai.controller;

import dasoni_backend.global.ai.dto.VoiceScriptDTO;
import dasoni_backend.global.ai.dto.VoiceScriptDTO.VoiceScriptRequestDTO;
import dasoni_backend.global.ai.dto.VoiceScriptDTO.VoiceScriptResponseDTO;
import dasoni_backend.global.ai.service.GeminiVoiceScriptServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiVoiceController {

    private final GeminiVoiceScriptServiceImpl geminiVoiceScriptService;

    @PostMapping("/voice-script")
    public VoiceScriptResponseDTO createVoiceScript(@RequestBody VoiceScriptRequestDTO dto) {
        String script = geminiVoiceScriptService.generateVoiceScript(dto.getLetterContent());
        return new VoiceScriptResponseDTO(script);
    }
}
