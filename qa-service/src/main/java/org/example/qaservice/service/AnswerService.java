// AnswerService.java
package org.example.qaservice.service;

import org.example.qacommon.entity.DTO.AnswerCreateDTO;
import org.example.qacommon.entity.DTO.AnswerResponseDTO;
import org.example.qacommon.entity.DeletionResult;

import java.util.List;

public interface AnswerService {
    AnswerResponseDTO createAnswer(String questionId, AnswerCreateDTO dto);

    List<AnswerResponseDTO> getAnswersByQuestionId(String questionId);

    AnswerResponseDTO getAnswerById(String questionId, String answerId);

    void incrementUpvotes(String questionId, String answerId, String userId);

    AnswerResponseDTO getTopAnswerByUpvotes(String questionId);

    DeletionResult deleteAnswer(String questionId, String answerId);

    void decrementUpvotes(String questionId, String answerId, String userId);

    List<AnswerResponseDTO> getAnswersByKnowledgePoint(String knowledgeId);
}