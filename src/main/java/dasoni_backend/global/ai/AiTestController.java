package dasoni_backend.global.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final GoogleGenAiChatModel chatModel;

    @GetMapping("/ai/test")
    public String test() {
        return chatModel.call("해원이 오늘 열심히 개발하고 있다고 두 줄로 응원해줘.");
    }
}
