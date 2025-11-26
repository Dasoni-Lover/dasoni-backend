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

    public String generateVoiceReplyScript(String currentLetterContent,
                                           String p1Emotion,
                                           String p2Emotion,
                                           String relationship,           // R
                                           String deceasedInsight,        // I
                                           String tone,                   // T (존댓말/반말)
                                           String frequentWords,          // W
                                           List<String> userDescriptions // A (3단어)
    ) {

        String userDescriptionsJoined = String.join(", ", userDescriptions);

        // null 방지
        if (p1Emotion == null) p1Emotion = "";
        if (p2Emotion == null) p2Emotion = "";

        String template = """
                ```scss
                [역할 정의]
                당신은 고인의 음성을 클로닝하여 사용자에게 전달할 음성 편지(답장)의 텍스트를 생성하는 AI입니다. 
                생성된 텍스트는 구어체 음성 합성 엔진을 통해 실제 말하는 것처럼 변환될 것이므로, 자연스러운 구어체와 감정 전달에 중점을 두고 답장을 작성해야 합니다.

                [필수 제약 조건]
                - 고인이 살아있는 것처럼 답장을 생성해서는 안 됩니다. 
                - 고인의 부재를 전제로 하되, 영원히 기억하고 있다는 뉘앙스를 유지합니다.
                - 답장의 마지막 멘트는 반드시 사용자가 일상 속에서 열심히 살아가도록 격려하고 현실에 집중하게 하는 내용이어야 합니다.
                - 최근 1개월 이내 편지 2개(P1, P2)는 이미 '감정 요약'만 남긴 상태이며, 원문 내용을 직접 인용하거나 구체적인 사건을 재구성해서는 안 됩니다.
                - 고인과 사용자가 주로 사용하던 말투/어투 (T)를 일관되게 적용하며, 고인의 자주 쓰던 말 (W)을 자연스럽게 포함합니다.
                - 문어체 대신 구어체를 사용하고, 끊김 없이 자연스럽게 이어지는 대화 흐름을 유지합니다.

                [참고 정보]
                관계 (R): 고인과 사용자의 관계 ({relationship})
                고인의 인식 (I): 고인이 사용자에 대해 알려준 내용 ({deceasedInsight})
                말투 (T): 주로 쓰던 말투 ([존댓말/반말] 중 택 1) ({tone})
                자주 쓰던 말 (W): 고인이 자주 사용하던 말이나 단어 ({frequentWords})
                사용자의 표현 (A): 사용자가 고인을 표현한 3가지 단어 ({userDescriptions})

                [최근 1개월 내 편지 원문 (P1, P2)]
                - P1: {p1}
                - P2: {p2}
                
                위 P1, P2는 '최근 편지의 원문'이지만,
                답변을 생성할 때는 이 텍스트에서 감정 상태만 추출해서 참고하시오.
                편지의 구체적인 내용, 사건, 날짜, 약속, 이름, 표현 등을
                직접 언급하거나 재구성해서는 안 됩니다.
                

                [답장 대상 편지 (P_reply)]
                "{currentLetter}"
                ```

                - 지침: P_reply 텍스트를 중심으로 답변하되, P1과 P2의 감정 요약을 참고하여 전체적인 말투와 위로의 깊이를 조절하시오.
                - 원칙: 사용자가 과거에 머무르지 않고 현재의 삶을 잘 살아갈 수 있도록, 다정하지만 현실적인 시선으로 응원하는 답장을 구성하시오.

                [AI 생성 답장]
                [답장 끝]
                """;



        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(
                Map.of(
                        "relationship", relationship,
                        "deceasedInsight", deceasedInsight,
                        "tone", tone,
                        "frequentWords", frequentWords,
                        "userDescriptions", userDescriptionsJoined,
                        "p1", p1Emotion,
                        "p2", p2Emotion,
                        "currentLetter", currentLetterContent
                )
        );

        return chatModel.call(prompt).getResult().getOutput().getText();
    }





//    public String generateVoiceReplyScript(String currentLetterContent,          // P_reply
//                                           List<String> recentLetterContents,    // P1, P2 (감정선 분석용)
//                                           double E_Current_Score,               // 현재 감정선 점수
//                                           double E_Depth_Score,                 // 심층 감정 점수
//                                           String R,                              // 관계
//                                           String I,                              // 고인의 인식
//                                           String T,                              // 말투
//                                           String W,                              // 자주 쓰던 말
//                                           String A                               // 사용자가 고인을 표현한 단어 3개
//                                           ) {
//
//        String template = """
//                너는 고인이 남긴 말투와 최근 편지들의 감정 흐름을 참고해서,
//                이번 편지에 대한 '음성으로 읽기 좋은 답장 스크립트'를 만드는 역할이야.
//
//                규칙:
//                - 아래 '최근 편지들'은 '기억 내용'을 그대로 말하지 말고, 감정의 방향성만 참고해.
//                - 문장 단위로 과거 편지를 인용하거나, 특정 사건/날짜/약속을 다시 구체적으로 언급하지 마.
//                - 이번 편지를 중심으로 답장을 작성해.
//                - 감정은 담되 과하지 않게.
//                - 구어체로 자연스럽게.
//                - 존댓말 유지.
//                - 너무 길면 자연스럽게 요약해서 10문장 이내로.
//                - 말투는 따뜻하고 차분하게.
//                - 문장 사이에는 말할 때 쉬어가는 느낌으로 적당히 끊어줘.
//
//                [최근 편지들 - 감정 방향성 참고용]
//                {recentLetters}
//
//                [이번 편지 - 답장의 핵심 대상]
//                {currentLetter}
//
//                변환된 음성 답장 스크립트:
//                """;
//
//        String recentJoined = String.join("\n---\n", recentLetterContents);
//
//        PromptTemplate promptTemplate = new PromptTemplate(template);
//        Prompt prompt = promptTemplate.create(
//                Map.of(
//                        "recentLetters", recentJoined,
//                        "currentLetter", currentLetterContent
//                )
//        );
//
//        return chatModel.call(prompt).getResult().getOutput().getText();
//    }
}
