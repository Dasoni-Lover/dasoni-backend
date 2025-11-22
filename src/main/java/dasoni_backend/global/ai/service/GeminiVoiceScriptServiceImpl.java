package dasoni_backend.global.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiVoiceScriptServiceImpl {

    private final GoogleGenAiChatModel chatModel;
//    테스트용
//    public String generateVoiceScript(String letterContent) {
//        String template = """
//                너는 편지를 '음성으로 읽기 좋은 스크립트'로 변환하는 역할이야.
//                요구사항:
//                - 감정은 담되 과하지 않게.
//                - 구어체로 자연스럽게.
//                - 너무 길면 짧게 정리.
//                - 존댓말 유지.
//                - 문장 사이 약간의 쉬는 느낌을 넣기 위해 적절히 끊어줘.
//                - 말투는 따뜻하고 차분하게.
//
//                원문:
//                {content}
//
//                변환된 음성 스크립트:
//                """;
//
//        PromptTemplate promptTemplate = new PromptTemplate(template);
//        Prompt prompt = promptTemplate.create(Map.of("content", letterContent));
//
//        return chatModel.call(prompt).getResult().getOutput().getContent();
//
//    }

    public String generateVoiceReplyScript(String currentLetterContent,
                                           List<String> recentLetterContents) {

        String template = """
                너는 고인이 남긴 말투와 최근 편지들의 감정 흐름을 참고해서,
                이번 편지에 대한 '음성으로 읽기 좋은 답장 스크립트'를 만드는 역할이야.

                규칙:
                - 아래 '최근 편지들'은 '기억 내용'을 그대로 말하지 말고, 감정의 방향성만 참고해.
                - 문장 단위로 과거 편지를 인용하거나, 특정 사건/날짜/약속을 다시 구체적으로 언급하지 마.
                - 이번 편지를 중심으로 답장을 작성해.
                - 감정은 담되 과하지 않게.
                - 구어체로 자연스럽게.
                - 존댓말 유지.
                - 너무 길면 자연스럽게 요약해서 10문장 이내로.
                - 말투는 따뜻하고 차분하게.
                - 문장 사이에는 말할 때 쉬어가는 느낌으로 적당히 끊어줘.

                [최근 편지들 - 감정 방향성 참고용]
                {recentLetters}

                [이번 편지 - 답장의 핵심 대상]
                {currentLetter}

                변환된 음성 답장 스크립트:
                """;

        String recentJoined = String.join("\n---\n", recentLetterContents);

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(
                Map.of(
                        "recentLetters", recentJoined,
                        "currentLetter", currentLetterContent
                )
        );

        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
