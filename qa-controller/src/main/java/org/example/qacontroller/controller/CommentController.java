package org.example.qacontroller.controller;


import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.qaservice.entity.Comment;
import org.example.qaservice.entity.DTO.CommentCreateDTO;
import org.example.qaservice.entity.DTO.CommentResponseDTO;
import org.example.qaservice.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions/{questionId}/answers/{answerId}/comments")
public class CommentController {

    @DubboReference
    private CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO createComment(
            @PathVariable String answerId,
            @RequestBody @Valid CommentCreateDTO commentDTO) {
        return commentService.createComment(answerId, commentDTO);
    }

    @GetMapping
    public List<CommentResponseDTO> getComments(@PathVariable String answerId) {
        return commentService.getCommentsByAnswer(answerId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @PathVariable String commentId) {
        commentService.deleteComment(questionId, answerId, commentId);
    }

    @PostMapping("/{commentId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upvoteComment(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @PathVariable String commentId,
            @RequestHeader("X-User-Id") String userId) {
        commentService.incrementUpvotes(questionId, answerId, commentId, userId);
    }

    @DeleteMapping("/{commentId}/upvotes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelUpvoteComment(
            @PathVariable String questionId,
            @PathVariable String answerId,
            @PathVariable String commentId,
            @RequestHeader("X-User-Id") String userId) {
        commentService.decrementUpvotes(questionId, answerId, commentId, userId);
    }

    @GetMapping("/top")
    public CommentResponseDTO getTopComment(
            @PathVariable String questionId,
            @PathVariable String answerId) {
        return commentService.getTopCommentByAnswer(questionId, answerId);
    }
}

