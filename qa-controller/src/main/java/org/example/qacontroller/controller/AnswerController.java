package org.example.qacontroller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "回答管理", description = "问题回答相关的API")
public class AnswerController {

    @DubboReference
    private AnswerService answerService;

    @DubboReference
    private QuestionService questionService;

    @DubboReference
    private AiEvaluationServiceForAw aiEvaluationServiceForAw;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建回答", description = "为指定问题创建新回答")
    @ApiResponse(responseCode = "201", description = "回答创建成功", content = @Content(schema = @Schema(implementation = AnswerResponseDTO.class)))
    public AnswerResponseDTO createAnswer(
            @Parameter(description = "问题ID") @PathVariable String questionId,
            @RequestBody @Valid AnswerCreateDTO answerDTO) {
        return answerService.createAnswer(questionId, answerDTO);
    }

    @DeleteMapping("/{answerId}")
    @Operation(summary = "删除回答", description = "删除指定回答及其关联评论")
    @ApiResponse(responseCode = "200", description = "回答删除成功", content = @Content(schema = @Schema(implementation = DeletionResult.class)))
    public DeletionResult deleteAnswer(
            @Parameter(description = "问题ID") @PathVariable String questionId,
            @Parameter(description = "回答ID") @PathVariable String answerId) {
        return answerService.deleteAnswer(questionId, answerId);
    }

    @GetMapping
    @Operation(summary = "获取问题的所有回答", description = "获取指定问题下的所有回答")
    @ApiResponse(responseCode = "200", description = "成功获取回答列表")
    public List<AnswerResponseDTO> getAnswersByQuestionId(
            @Parameter(description = "问题ID") @PathVariable String questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @GetMapping("/{answerId}")
    @Operation(summary = "获取回答详情", description = "获取指定回答的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取回答详情"),
            @ApiResponse(responseCode = "404", description = "回答不存在"),
            @ApiResponse(responseCode = "400", description = "回答不属于指定问题")
    })
    public AnswerResponseDTO getAnswerById(
            @Parameter(description = "问题ID") @PathVariable String questionId,
            @Parameter(description = "回答ID") @PathVariable String answerId) {
        return answerService.getAnswerById(questionId, answerId);
    }

    @PostMapping("/{answerId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "点赞回答", description = "为回答增加点赞")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "点赞成功"),
            @ApiResponse(responseCode = "404", description = "回答不存在")
    })
    public void upvoteAnswer(
            @Parameter(description = "问题ID") @PathVariable String questionId,
            @Parameter(description = "回答ID") @PathVariable String answerId,
            @RequestHeader("X-User-Id") String userId) {
        answerService.incrementUpvotes(questionId, answerId, userId);
    }

    @DeleteMapping("/{answerId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "取消点赞", description = "取消对回答的点赞")
    @ApiResponse(responseCode = "204", description = "取消点赞成功")
    public void cancelUpvoteAnswer(
            @Parameter(description = "问题ID") @PathVariable String questionId,
            @Parameter(description = "回答ID") @PathVariable String answerId,
            @RequestHeader("X-User-Id") String userId) {
        answerService.decrementUpvotes(questionId, answerId, userId);
    }

    @GetMapping("/top")
    @Operation(summary = "获取最佳回答", description = "获取问题下点赞数最高的回答")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取最佳回答"),
            @ApiResponse(responseCode = "404", description = "无回答")
    })
    public AnswerResponseDTO getTopAnswer(
            @Parameter(description = "问题ID") @PathVariable String questionId) {
        return answerService.getTopAnswerByUpvotes(questionId);
    }

    @GetMapping("/by-knowledge/{knowledgeId}")
    @Operation(summary = "获取知识点相关回答", description = "获取与指定知识点相关的所有回答")
    @ApiResponse(responseCode = "200", description = "成功获取相关回答列表")
    public List<AnswerResponseDTO> getAnswersByKnowledge(
            @Parameter(description = "知识点ID") @PathVariable String knowledgeId) {
        return answerService.getAnswersByKnowledgePoint(knowledgeId);
    }

    @PostMapping("/{answerId}/ai-evaluations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "触发回答AI评估", description = "对回答进行AI质量评估")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "成功触发AI评估"),
            @ApiResponse(responseCode = "404", description = "问题或回答不存在"),
            @ApiResponse(responseCode = "500", description = "评估触发失败")
    })
    public AIForAw triggerAnswerAiEvaluation(
            @Parameter(description = "问题ID") @PathVariable String questionId,
            @Parameter(description = "回答ID") @PathVariable String answerId,
            @Parameter(description = "AI模型版本(可选)") @RequestParam(required = false) String modelVersion) {
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
