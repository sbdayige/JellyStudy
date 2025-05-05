package org.example.qaservice.service;

import org.bson.types.ObjectId;
import org.example.qacommon.entity.DTO.QuestionCreateDTO;
import org.example.qacommon.entity.DTO.QuestionDetailsDTO;
import org.example.qacommon.entity.DTO.QuestionResponseDTO;
import org.example.qacommon.entity.DeletionResult;
import org.example.qacommon.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface QuestionService {
    long getTotalQuestions();

    QuestionDetailsDTO getQuestionDetails(String questionId);

    QuestionResponseDTO getQuestionById(String questionId); // 返回 DTO

    QuestionResponseDTO createQuestion(QuestionCreateDTO dto); // 接受 DTO, 返回 DTO

    @Transactional
    DeletionResult deleteQuestion(ObjectId questionId);

    Page<QuestionResponseDTO> getNewestQuestions(int page, int size); // 返回 DTO Page

    QuestionResponseDTO getHottestQuestion(); // 返回 DTO

    void incrementUpvotes(String questionId, String userId);

    void decrementUpvotes(String questionId, String userId);

    // 内部使用，获取实体
    Question findQuestionEntityById(String questionId);
}