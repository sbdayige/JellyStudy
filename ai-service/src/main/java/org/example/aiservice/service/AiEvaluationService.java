package org.example.aiservice.service;

import org.example.aiservice.entity.AIForAw;
import org.example.aiservice.entity.AIForQa;

public interface AiEvaluationService {
    AIForQa evaluateQuestion(String questionId, String content, String modelVersion);

    AIForQa getEvaluationByQuestionId(String questionId);

    void deleteEvaluationById(String evaluationId);

    void deleteEvaluationsByQuestionId(String questionId);
}