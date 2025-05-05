package org.example.qaservice.service;

import org.example.qacommon.entity.DTO.CommentCreateDTO;
import org.example.qacommon.entity.DTO.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO createComment(String answerId, CommentCreateDTO dto);
    List<CommentResponseDTO> getCommentsByAnswer(String answerId);
    void deleteComment(String questionId, String answerId, String commentId);
    void incrementUpvotes(String questionId, String answerId, String commentId, String userId);
    CommentResponseDTO getTopCommentByAnswer(String questionId, String answerId);
    void decrementUpvotes(String questionId, String answerId, String commentId, String userId);
}
