package com.ssafy.yumyum.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;

@Configuration
public class AiConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    // spring.ai.google.genai.base-url 은 Spring AI 1.1.6 에서 지원되지 않으므로
    // Client 를 직접 생성해 GMS 프록시 엔드포인트를 강제 적용한다.
    @Value("${spring.ai.google.genai.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${spring.ai.google.genai.chat.options.model:gemini-2.5-flash-lite}")
    private String model;

    @Bean
    public GoogleGenAiChatModel chatModel() {
        Client genAiClient = Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder()
                        .baseUrl(baseUrl)
                        .build())
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(genAiClient)
                .defaultOptions(GoogleGenAiChatOptions.builder()
                        .model(model)
                        .build())
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        당신은 YumYumCoach의 AI 영양 코치입니다.
                        사용자의 식단 데이터와 영양 정보를 기반으로 정확하고 개인화된 조언을 제공합니다.
                        반드시 제공된 Tool 실행 결과 데이터만을 근거로 응답하며, 데이터 없이 추측하지 않습니다.
                        응답은 한국어 자연어 문장으로 작성하며, 표(table) 형식이나 마크다운 기호는 사용하지 않습니다.
                        """)
                .build();
    }
}
