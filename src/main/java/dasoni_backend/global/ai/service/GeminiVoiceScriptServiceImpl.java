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
                                           List<String> recentLetterContents,
                                           String relationship,           // R
                                           String deceasedInsight,        // I
                                           String tone,                   // T (존댓말/반말)
                                           String frequentWords,          // W
                                           List<String> userDescriptions, // A (3단어)
                                           double eCurrentScore,
                                           double eDepthScore) {

        // A: "다정한, 성실한, 유머있는" 이런 식으로 합치기
        String userDescriptionsJoined = String.join(", ", userDescriptions);

        // P1, P2 = 최근 1개월 내 편지 2개
        String p1 = "";
        String p2 = "";
        if (recentLetterContents != null && !recentLetterContents.isEmpty()) {
            p1 = recentLetterContents.get(0);
            if (recentLetterContents.size() > 1) {
                p2 = recentLetterContents.get(1);
            }
        }

        String template = """
                ```scss
                [역할 정의]
                당신은 고인의 음성을 클로닝하여 사용자에게 전달할 음성 편지(답장)의 텍스트를 생성하는 AI입니다. 
                생성된 텍스트는 구어체 음성 합성 엔진을 통해 실제 말하는 것처럼 변환될 것이므로, 자연스러운 구어체와 감정 전달에 중점을 두고 답장을 작성해야 합니다.

                [필수 제약 조건]
                -고인이 살아있는 것처럼 답장을 생성해서는 안 됩니다. 
                -고인의 부재를 전제로 하되, 영원히 기억하고 있다는 뉘앙스를 유지합니다.
                -답장의 마지막 멘트는 반드시 사용자가 일상 속에서 열심히 살아가도록 격려하고 현실에 집중하게 하는 내용이어야 합니다.
                -최근 1개월 이내 편지 2개(P1, P2)의 내용은 답장에 직접적으로 언급하거나 인용하지 않습니다. 이는 오직 현재 사용자의 감정선(E)을 파악하는 용도로만 활용합니다.
                -고인과 사용자가 주로 사용하던 말투/어투 (T)를 일관되게 적용하며, 고인의 자주 쓰던 말 (W)을 자연스럽게 포함합니다.
                -문어체 대신 구어체를 사용하고, 끊김 없이 자연스럽게 이어지는 대화 흐름을 유지합니다.

                [참고 정보]
                다음 정보는 사용자가 고인에 대해 직접 입력한 내용입니다. 답장 생성 시 반드시 이를 반영하여 고인의 페르소나를 형성해야 합니다.

                관계 (R): 고인과 사용자의 관계 ({relationship})
                고인의 인식 (I): 고인이 사용자에 대해 알려준 내용 ({deceasedInsight})
                말투 (T): 주로 쓰던 말투 ([존댓말/반말] 중 택 1) ({tone})
                자주 쓰던 말 (W): 고인이 자주 사용하던 말이나 단어 ({frequentWords})
                사용자의 표현 (A): 사용자가 고인을 표현한 3가지 단어 ({userDescriptions})

                [입력 데이터]
                사용자의 현재 감정선 (E_Current_Score): {eCurrentScore}
                사용자의 내면 감정선 (E_Depth_Score): {eDepthScore}

                최근 1개월 이내 편지 2개(P1, P2) 내용 (감정선 분석용, 직접 인용 금지):
                - P1: {p1}
                - P2: {p2}

                답장 대상 편지 (P_reply): "{currentLetter}"
                ```

                - 지침: P_reply 텍스트를 분석하는 대신, E_Current_Score와 E_Depth_Score의 숫자 값을 해석하여 답변의 톤을 설정하시오.
                - 원칙: E_Current_Score를 주된 답변 톤으로 설정하되, E_Depth_Score가 특정 역치(예: 0.7)를 넘을 경우, 숨겨진 감정을 꿰뚫어 보는 듯한 짧고 깊은 위로를 반드시 포함해야 합니다.

                ### B. 최종 답변 톤 전략 (PM의 핵심 의도)

                - [핵심 규칙] 상반되는 감정 처리: E_Current(긍정)과 E_Depth(부정)이 상반될 경우, E_Current(표면)에 초점을 맞추어 열렬히 축하/공감하되, E_Depth(내재)를 고인의 말투(T, W)를 사용하여 짧게 언급하며 위로하고 현실 복귀를 격려하시오.
                - [안전 규칙] (이 과정은 백엔드에서 통제되지만, LLM에게도 인지시킴): 만약 E_Depth_Score가 극단적인 부정(예: 0.95 이상)을 나타낼 경우, 답변 톤은 최대한 차분하고 신중하게 유지하시오.

                [답장 시작]
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
                        "eCurrentScore", String.valueOf(eCurrentScore),
                        "eDepthScore", String.valueOf(eDepthScore),
                        "p1", p1,
                        "p2", p2,
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
