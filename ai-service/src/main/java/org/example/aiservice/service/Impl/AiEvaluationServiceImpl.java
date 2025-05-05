package org.example.aiservice.service.Impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.example.aicommon.entity.AIForQa;
import org.example.aiservice.repository.AIForQaRepository;
import org.example.aiservice.service.AiEvaluationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

@DubboService
@RequiredArgsConstructor
public class AiEvaluationServiceImpl implements AiEvaluationService {
    private final ChatClient.Builder chatClientBuilder; // 改为注入Builder
    private final AIForQaRepository aiQaRepo;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_QA_EVAL_MODEL = "qwen-max";

    // 统一配置的ChatClient
    private ChatClient createEvaluatorClient(String modelVersion) {
        String modelToUse = StringUtils.hasText(modelVersion) ? modelVersion : DEFAULT_QA_EVAL_MODEL;

        return chatClientBuilder
                .defaultSystem("你是一个专业的教育领域AI评估助手，请严格按JSON格式要求进行分析")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new SimpleLoggerAdvisor())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel(modelToUse) // 使用 modelToUse
                                .withTopP(0.8)
                                .build())
                .build();
    }

    @Override
    public AIForQa evaluateQuestion(String questionId, String content, String modelVersion) {
        String prompt = """
                请分析以下题目：
                %s

                要求返回JSON格式：
                {
                    "knowledge_points": ["知识点1", "知识点2"],
                    "difficulty_level": "EASY/MEDIUM/HARD",
                    "confidence_score": 0.95
                }
                """.formatted(content);

        // 使用传入的 modelVersion 创建 client
        ChatClient client = createEvaluatorClient(modelVersion);
        // 确定实际使用的模型版本
        String actualModelUsed = StringUtils.hasText(modelVersion) ? modelVersion : DEFAULT_QA_EVAL_MODEL;

        try {
            String jsonResponse = client.prompt()
                    .user(prompt)
                    .call()
                    .content();

            AIForQa evaluation = parseResponse(jsonResponse);
            evaluation.setQuestionId(questionId);
            evaluation.setModelVersion(actualModelUsed); // 设置实际使用的模型版本
            return aiQaRepo.save(evaluation);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI响应解析失败", e);
        } catch (Exception e) { // 捕获更广泛的异常，例如调用AI服务失败
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "调用AI评估服务失败", e);
        }
    }

    @Override
    public AIForQa getEvaluationByQuestionId(String questionId) {
        return aiQaRepo.findByQuestionId(questionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "未找到 Question ID 为 " + questionId + " 的 AI 评估记录"));
    }

    private AIForQa parseResponse(String json) throws JsonProcessingException {
        return objectMapper.readerFor(AIForQa.class)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // 忽略未知字段
                .readValue(json);
    }

    @Override
    public void deleteEvaluationById(String evaluationId) {
        try {
            if (!aiQaRepo.existsById(evaluationId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "AI评测记录不存在");
            }
            aiQaRepo.deleteById(evaluationId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的评测ID格式");
        }
    }

    @Override
    public void deleteEvaluationsByQuestionId(String questionId) {
        aiQaRepo.deleteByQuestionId(questionId); // 需要Repository支持该方法
    }

}
