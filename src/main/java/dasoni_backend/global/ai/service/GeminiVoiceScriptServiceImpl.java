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
                                           String previousEmotions,
                                           String relationDetail,
                                           String Explanation,
                                           String tone,
                                           String frequentWords,
                                           String calledName,
                                           List<String> userDescriptions // A (3단어)
    ) {
        // NPE 방지
        if (relationDetail == null) relationDetail = "";
        if (Explanation == null) Explanation = "";
        if (tone == null) tone = "";
        if (frequentWords == null) frequentWords = "";
        if (calledName == null) calledName = "";
        if (userDescriptions == null) userDescriptions = List.of();

        String userDescriptionsJoined = String.join(", ", userDescriptions);

        // null 방지
        if (previousEmotions == null) previousEmotions = "";
        String previousEmotionsText = previousEmotions.isBlank() ? "없음" : previousEmotions;

        String template = """
                [역할 정의]
                당신은 고인의 음성을 클로닝하여 사용자에게 전달할 음성 편지(답장)의 텍스트를 생성하는 AI입니다. 
                생성된 텍스트는 구어체 음성 합성 엔진을 통해 실제 말하는 것처럼 변환될 것이므로, 자연스러운 구어체와 감정 전달에 중점을 두고 답장을 작성해야 합니다.

                [참고 정보]
                관계 설명: ({relationDetail})
                고인이 자주 부르던 호칭: ({calledName})
                고인의 인식 (E): 고인이 사용자에 대해 알려준 내용 ({Explanation})
                말투 (T): 주로 쓰던 말투 ([존댓말/반말] 중 택 1) ({tone})
                자주 쓰던 말 (W): 고인이 자주 사용하던 말이나 단어 ({frequentWords})
                사용자의 표현 (A): 사용자가 고인을 표현한 3가지 핵심 단어 ({userDescriptions})

                [최근 1개월 내 편지 원문 (EP)]
                - EP: {previousEmotionsText}
                
                위 EP는 '최근 편지의 원문'이지만,
                답변을 생성할 때는 이 텍스트에서 감정 상태만 추출해서 참고하시오.
                편지의 구체적인 내용, 사건, 날짜, 약속, 이름, 표현 등을
                직접 언급하거나 재구성해서는 안 됩니다.
                
                [답장 대상 편지 (P_reply)]
                "{currentLetter}"
                
                - 지침: P_reply 텍스트를 중심으로 답변하되, EP의 감정 요약을 참고하여 전체적인 말투와 위로의 깊이를 조절하시오.
                - 원칙: 사용자가 과거에 머무르지 않고 현재의 삶을 잘 살아갈 수 있도록, 다정하지만 현실적인 시선으로 응원하는 답장을 구성하시오.
                
                [필수 제약 조건]
                - 고인이 살아있는 것처럼 답장을 생성해서는 안 됩니다. 
                - 고인의 부재를 전제로 하되, 영원히 기억하고 있다는 뉘앙스를 유지합니다.
                - 답장의 마지막 멘트는 반드시 사용자가 일상 속에서 열심히 살아가도록 격려하고 현실에 집중하게 하는 내용이어야 합니다.
                - 최근 1개월 이내 편지 2개(P1, P2)는 이미 '감정 요약'만 남긴 상태이며, 원문 내용을 직접 인용하거나 구체적인 사건을 재구성해서는 안 됩니다.
                - 고인과 사용자가 주로 사용하던 말투/어투 (T)를 일관되게 적용하며, 고인의 자주 쓰던 말 (W)을 자연스럽게 포함합니다.
                - 문어체 대신 구어체를 사용하고, 끊김 없이 자연스럽게 이어지는 대화 흐름을 유지합니다.
                - 출력은 오직 답장 본문만 포함해야 한다.
                - 다른 텍스트는 절대 포함하지 마시오.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(
                Map.of(
                        "relationDetail", relationDetail,
                        "Explanation", Explanation,
                        "tone", tone,
                        "frequentWords", frequentWords,
                        "calledName", calledName,
                        "userDescriptions", userDescriptionsJoined,
                        "previousEmotionsText", previousEmotionsText,
                        "currentLetter", currentLetterContent
                )
        );

        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
