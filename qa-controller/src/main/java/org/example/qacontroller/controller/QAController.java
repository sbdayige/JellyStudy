package org.example.qacontroller.controller;

import jakarta.validation.Valid; // 导入 Valid
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.example.aicommon.entity.AIForQa;
import org.example.aiservice.service.AiEvaluationService;
import org.example.qacommon.entity.DTO.QuestionCreateDTO;
import org.example.qacommon.entity.DTO.QuestionDetailsDTO;
import org.example.qacommon.entity.DTO.QuestionResponseDTO;
import org.example.qacommon.entity.DeletionResult;
import org.example.qacommon.entity.Question;
import org.example.qaservice.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/questions")
public class QAController {

    @DubboReference
    private QuestionService questionService;
    @DubboReference
    private AiEvaluationService aiEvaluationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponseDTO createQuestion(@RequestBody @Valid QuestionCreateDTO questionDTO) { // 接受 DTO, 返回 DTO
        return questionService.createQuestion(questionDTO);
    }

    @GetMapping("/{questionId}")
    public QuestionResponseDTO getQuestion(@PathVariable String questionId) { // 返回 DTO
        try {
            return questionService.getQuestionById(questionId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid question ID format");
        } catch (ResponseStatusException e) { // 捕获 Service 抛出的 NOT_FOUND
            throw e;
        }
    }

    @DeleteMapping("/{questionId}")
    public DeletionResult deleteQuestion(@PathVariable String questionId) {
        try {
            return questionService.deleteQuestion(new ObjectId(questionId));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的问题ID格式：" + questionId);
        }
    }

    @GetMapping("/hottest")
    public ResponseEntity<QuestionResponseDTO> getHottestQuestion() { // 返回 DTO
        try {
            return ResponseEntity.ok(questionService.getHottestQuestion());
        } catch (ResponseStatusException e) {
            // 如果 Service 抛出 NOT_FOUND，则返回 404
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @PostMapping("/{questionId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upvoteQuestion(@PathVariable String questionId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            questionService.incrementUpvotes(questionId, userId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的问题ID格式");
        } catch (ResponseStatusException e) { // 捕获 Service 抛出的 NOT_FOUND
            throw e;
        }
    }

    @DeleteMapping("/{questionId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelUpvoteQuestion(
            @PathVariable String questionId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            questionService.decrementUpvotes(questionId, userId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的ID格式");
        } catch (ResponseStatusException e) { // 捕获 Service 可能抛出的异常
            throw e;
        }
    }

    @GetMapping("/count")
    public long getTotalQuestions() {
        return questionService.getTotalQuestions();
    }

    @GetMapping("/{questionId}/details")
    public QuestionDetailsDTO getQuestionDetails(@PathVariable String questionId) {
        try {
            return questionService.getQuestionDetails(questionId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        } catch (ResponseStatusException e) {
            throw e;
        }
    }

    @PostMapping("/{questionId}/ai-evaluations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AIForQa triggerAiEvaluation(
            @PathVariable String questionId,
            @RequestParam(required = false) String modelVersion) { // modelVersion 接收参数
        try {
            Question questionEntity = questionService.findQuestionEntityById(questionId);
            return aiEvaluationService.evaluateQuestion(
                    questionId,
                    questionEntity.getQuestionText(),
                    modelVersion
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的问题ID格式");
        } catch (ResponseStatusException e) {
            throw e;
        }
    }
}