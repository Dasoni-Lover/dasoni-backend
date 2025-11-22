package dasoni_backend.global.ai.controller;

import dasoni_backend.domain.relationship.repository.RelationshipRepository;
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
    private final RelationshipRepository relationshipRepository;
//    @PostMapping("/voice-script")
//    public VoiceScriptResponseDTO createVoiceScript(@RequestBody VoiceScriptRequestDTO request) {
//        String script = geminiVoiceScriptService.generateVoiceReplyScript(request.getCurrentLetterContent(), request.getRecentLetterContents());
//        return new VoiceScriptResponseDTO(script);
//    }

    @PostMapping("/voice-script")
    public VoiceScriptResponseDTO createVoiceScript(@RequestBody VoiceScriptRequestDTO request) {

        String script = geminiVoiceScriptService.generateVoiceReplyScript(
                request.getCurrentLetterContent(),
                request.getRecentLetterContents(),
                request.getRelationship(),
                request.getDeceasedInsight(),
                request.getTone(),
                request.getFrequentWords(),
                request.getUserDescriptions(),
                request.getECurrentScore(),
                request.getEDepthScore()
        );

        return new VoiceScriptResponseDTO(script);
    }

}
