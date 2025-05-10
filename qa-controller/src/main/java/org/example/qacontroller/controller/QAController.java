package org.example.qacontroller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "问题管理", description = "问题相关的API接口")
public class QAController {

    @DubboReference
    private QuestionService questionService;
    @DubboReference
    private AiEvaluationService aiEvaluationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建问题", description = "创建新问题并返回问题信息")
    @ApiResponse(responseCode = "201", description = "问题创建成功", content = @Content(schema = @Schema(implementation = QuestionResponseDTO.class)))
    public QuestionResponseDTO createQuestion(@RequestBody @Valid QuestionCreateDTO questionDTO) { // 接受 DTO, 返回 DTO
        return questionService.createQuestion(questionDTO);
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "获取问题", description = "根据ID获取问题详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取问题", content = @Content(schema = @Schema(implementation = QuestionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "问题不存在"),
            @ApiResponse(responseCode = "400", description = "无效的问题ID格式")
    })
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
    @Operation(summary = "删除问题", description = "删除指定问题及其关联的回答和评论")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "问题删除成功", content = @Content(schema = @Schema(implementation = DeletionResult.class))),
            @ApiResponse(responseCode = "400", description = "无效的问题ID格式")
    })
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
    @Operation(summary = "获取最热门问题", description = "获取热度评分最高的问题")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取热点问题", content = @Content(schema = @Schema(implementation = QuestionResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "没有热点问题")
    })
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
    @Operation(summary = "点赞问题", description = "为问题增加点赞")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "点赞成功"),
            @ApiResponse(responseCode = "404", description = "问题不存在"),
            @ApiResponse(responseCode = "400", description = "无效的问题ID格式")
    })
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
    @Operation(summary = "取消点赞", description = "取消对问题的点赞")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "取消点赞成功"),
            @ApiResponse(responseCode = "400", description = "无效的问题ID格式")
    })
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
    @Operation(summary = "获取问题总数", description = "获取系统中问题的总数量")
    @ApiResponse(responseCode = "200", description = "成功获取问题总数")
    public long getTotalQuestions() {
        return questionService.getTotalQuestions();
    }

    @GetMapping("/{questionId}/details")
    @Operation(summary = "获取问题详情", description = "获取问题及其关联回答和评论的完整详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取问题详情", content = @Content(schema = @Schema(implementation = QuestionDetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "问题不存在"),
            @ApiResponse(responseCode = "400", description = "无效的ID格式")
    })
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
    @Operation(summary = "触发AI评估", description = "对问题进行AI质量评估")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "成功触发AI评估", content = @Content(schema = @Schema(implementation = AIForQa.class))),
            @ApiResponse(responseCode = "404", description = "问题不存在"),
            @ApiResponse(responseCode = "400", description = "无效的问题ID格式")
    })
    public AIForQa triggerAiEvaluation(
            @PathVariable String questionId,
            @RequestParam(required = false) String modelVersion) { // modelVersion 接收参数
        try {
            Question questionEntity = questionService.findQuestionEntityById(questionId);
            return aiEvaluationService.evaluateQuestion(
                    questionId,
                    questionEntity.getQuestionText(),
                    modelVersion);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的问题ID格式");
        } catch (ResponseStatusException e) {
            throw e;
        }
    }
}