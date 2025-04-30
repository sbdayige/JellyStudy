// filepath: c:\Users\guoshenshen\Desktop\study\yyjg\class8\code\ai-service\src\main\java\org\example\aiservice\service\Impl\AiEvaluationServiceForAwImpl.java
package org.example.aiservice.service.Impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.example.aiservice.entity.AIForAw;
import org.example.aiservice.repository.AIForAwRepository;
import org.example.aiservice.service.AiEvaluationServiceForAw;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@DubboService
@RequiredArgsConstructor
public class AiEvaluationServiceForAwImpl implements AiEvaluationServiceForAw {

    private final ChatClient.Builder chatClientBuilder;
    private final AIForAwRepository aiAwRepo;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_AW_EVAL_MODEL = "qwen-plus"; // 定义回答评估的默认模型

    // 修改 createEvaluatorClient 以接受可选的 modelVersion
    private ChatClient createEvaluatorClient(String modelVersion) {
        String modelToUse = StringUtils.hasText(modelVersion) ? modelVersion : DEFAULT_AW_EVAL_MODEL;

        return chatClientBuilder
                .defaultSystem("你是一个专业的教育领域AI评估助手，请严格按JSON格式要求进行分析")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new SimpleLoggerAdvisor())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel(modelToUse) // 使用 modelToUse
                                .withTopP(0.8) // 保留原有的 topP 设置
                                .build())
                .build();
    }

    private AIForAw parseAwResponse(String json) throws JsonProcessingException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(json, AIForAw.class);
    }

    // 更新 evaluateAnswer 方法签名 (已在之前步骤完成，此处确认)
    @Override
    public AIForAw evaluateAnswer(String questionId, String answerId, String questionContent, String answerContent,
            String modelVersion) {
        String prompt = """
                请根据以下问题和回答，对回答的质量进行评估。
                请遵循100分制进行打分，并提供具体的反馈意见和评估摘要。

                问题：
                %s

                回答：
                %s

                请以JSON格式返回评估结果，包含以下字段：
                {
                    "quality_score": 85.5, // 质量评分 (0-100的浮点数)
                    "feedbacks": ["反馈意见1", "反馈意见2"], // 具体的改进建议列表
                    "evaluation_summary": "评估摘要总结" // 对回答质量的整体评价
                }
                """.formatted(questionContent, answerContent);

        // 使用传入的 modelVersion 创建 client
        ChatClient client = createEvaluatorClient(modelVersion);
        // 确定实际使用的模型版本
        String actualModelUsed = StringUtils.hasText(modelVersion) ? modelVersion : DEFAULT_AW_EVAL_MODEL;

        try {
            String jsonResponse = client.prompt()
                    .user(prompt)
                    .call()
                    .content();

            AIForAw evaluation = parseAwResponse(jsonResponse);
            evaluation.setQuestionId(questionId);
            evaluation.setAnswerId(answerId);
            evaluation.setEvaluatedAt(LocalDateTime.now());
            evaluation.setModelVersion(actualModelUsed); // 设置实际使用的模型版本

            return aiAwRepo.save(evaluation);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AI回答评估响应解析失败", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "调用AI评估服务失败", e);
        }
    }

    @Override
    public List<AIForAw> getEvaluationsByAnswerId(String answerId) {
        return aiAwRepo.findByAnswerId(answerId);
    }

    @Override
    public List<AIForAw> getAnswerEvaluationsByQuestionId(String questionId) {
        return aiAwRepo.findByQuestionId(questionId);
    }

    @Override
    public void deleteAnswerEvaluationById(String evaluationId) {
        try {
            if (!aiAwRepo.existsById(evaluationId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "未找到 ID 为 " + evaluationId + " 的回答 AI 评估记录");
            }
            aiAwRepo.deleteById(evaluationId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的评估 ID 格式: " + evaluationId);
        }
    }

    @Override
    public void deleteEvaluationsByAnswerId(String answerId) {
        // 直接调用 Repository 的方法删除所有匹配 answerId 的文档
        aiAwRepo.deleteByAnswerId(answerId);
        // 可以添加日志记录删除操作
        // log.info("Deleted AI evaluations for answerId: {}", answerId);
    }
}