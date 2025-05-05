package org.example.qacontroller.controller;

import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.aicommon.entity.AIForAw;
import org.example.aiservice.service.AiEvaluationServiceForAw;
import org.example.qacommon.entity.DeletionResult;
import org.example.qacommon.entity.Question;
import org.example.qacommon.entity.DTO.AnswerCreateDTO;
import org.example.qacommon.entity.DTO.AnswerResponseDTO;
import org.example.qaservice.service.AnswerService;
import org.example.qaservice.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerController {

    @DubboReference
    private AnswerService answerService;

    @DubboReference // 注入 QuestionService
    private QuestionService questionService;

    @DubboReference // 注入 AiEvaluationServiceForAw
    private AiEvaluationServiceForAw aiEvaluationServiceForAw;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerResponseDTO createAnswer(
            @PathVariable String questionId,
            @RequestBody @Valid AnswerCreateDTO answerDTO) {
        return answerService.createAnswer(questionId, answerDTO);
    }

    @DeleteMapping("/{answerId}")
    public DeletionResult deleteAnswer(
            @PathVariable String questionId,
            @PathVariable String answerId) {
        return answerService.deleteAnswer(questionId, answerId);
    }

    @GetMapping
    public List<AnswerResponseDTO> getAnswersByQuestionId(
            @PathVariable String questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @GetMapping("/{answerId}")
    public AnswerResponseDTO getAnswerById(
            @PathVariable String questionId,
            @PathVariable String answerId) {
        return answerService.getAnswerById(questionId, answerId);
    }

    @PostMapping("/{answerId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upvoteAnswer(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @RequestHeader("X-User-Id") String userId) {
        answerService.incrementUpvotes(questionId, answerId, userId);
    }

    @DeleteMapping("/{answerId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelUpvoteAnswer(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @RequestHeader("X-User-Id") String userId) {
        answerService.decrementUpvotes(questionId, answerId, userId);
    }

    @GetMapping("/top")
    public AnswerResponseDTO getTopAnswer(
            @PathVariable String questionId) {
        return answerService.getTopAnswerByUpvotes(questionId);
    }

    @GetMapping("/by-knowledge/{knowledgeId}")
    public List<AnswerResponseDTO> getAnswersByKnowledge(
            @PathVariable String knowledgeId) {
        return answerService.getAnswersByKnowledgePoint(knowledgeId);
    }

    @PostMapping("/{answerId}/ai-evaluations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AIForAw triggerAnswerAiEvaluation(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @RequestParam(required = false) String modelVersion) {
        try {
            // 获取问题文本
            Question questionEntity = questionService.findQuestionEntityById(questionId);
            // 获取回答内容
            AnswerResponseDTO answerDTO = answerService.getAnswerById(questionId, answerId);

            // 调用 AI 评估服务
            return aiEvaluationServiceForAw.evaluateAnswer(
                    questionId,
                    answerId,
                    questionEntity.getQuestionText(),
                    answerDTO.getContent(),
                    modelVersion);
        } catch (ResponseStatusException e) {
            throw e; // 重新抛出已知的状态异常
        } catch (Exception e) {
            // 处理其他意外错误
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "触发回答AI评估失败: " + e.getMessage(), e);
        }
    }
}
