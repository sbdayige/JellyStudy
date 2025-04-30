package org.example.aicontroller.controller;

import java.util.List;

import org.apache.dubbo.config.annotation.DubboReference;
import org.example.aiservice.entity.AIForAw;
import org.example.aiservice.entity.AIForQa;
import org.example.aiservice.service.AiEvaluationService;
import org.example.aiservice.service.AiEvaluationServiceForAw;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * AI 评估控制器
 * 用于管理问题和回答的 AI 评估
 */
@RestController
@RequestMapping("/api/ai-evaluations")
public class AIEvaluationController {

    @DubboReference
    private AiEvaluationService aiEvaluationService;

    @DubboReference // 注入 AiEvaluationServiceForAw
    private AiEvaluationServiceForAw aiEvaluationServiceForAw;

    @DeleteMapping("/{evaluationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvaluation(@PathVariable String evaluationId) {
        try {
            aiEvaluationService.deleteEvaluationById(evaluationId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "删除 AI 评估时发生错误: " + e.getMessage());
        }
    }

    @GetMapping("/by-question/{questionId}")
    public AIForQa getEvaluationByQuestionId(@PathVariable String questionId) {
        try {
            return aiEvaluationService.getEvaluationByQuestionId(questionId);
        } catch (ResponseStatusException e) {
            // 直接抛出 Service 层可能抛出的 NOT_FOUND 等异常
            throw e;
        } catch (Exception e) {
            // 捕获其他未知异常
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "查询 AI 评估时发生错误: " + e.getMessage(), e);
        }
    }

    @GetMapping("/by-answer/{answerId}")
    public List<AIForAw> getEvaluationsByAnswerId(@PathVariable String answerId) {
        try {
            return aiEvaluationServiceForAw.getEvaluationsByAnswerId(answerId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "查询回答评估时出错: " + e.getMessage(), e);
        }
    }

    @GetMapping("/by-question/{questionId}/answers")
    public List<AIForAw> getAnswerEvaluationsByQuestionId(@PathVariable String questionId) {
        try {
            return aiEvaluationServiceForAw.getAnswerEvaluationsByQuestionId(questionId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "查询问题下回答评估时出错: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/answers/{evaluationId}") // 使用特定路径区分
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswerEvaluation(@PathVariable String evaluationId) {
        try {
            aiEvaluationServiceForAw.deleteAnswerEvaluationById(evaluationId);
        } catch (ResponseStatusException e) {
            // 直接抛出 Service 层可能抛出的 NOT_FOUND, BAD_REQUEST 等异常
            throw e;
        } catch (Exception e) {
            // 捕获其他未知异常
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "删除回答 AI 评估时发生错误: " + e.getMessage(), e);
        }
    }
}