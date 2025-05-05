// filepath: c:\Users\guoshenshen\Desktop\study\yyjg\class8\code\ai-service\src\main\java\org\example\aiservice\service\AiEvaluationServiceForAw.java
package org.example.aiservice.service;

import org.example.aicommon.entity.AIForAw;
import java.util.List; // 导入 List

public interface AiEvaluationServiceForAw {

    // 修改签名，加入 questionId
    AIForAw evaluateAnswer(String questionId, String answerId, String questionContent, String answerContent,
            String modelVersion);

    // 新增：根据 Answer ID 查询评估列表
    List<AIForAw> getEvaluationsByAnswerId(String answerId);

    // 新增：根据 Question ID 查询所有相关回答的评估列表
    List<AIForAw> getAnswerEvaluationsByQuestionId(String questionId);

    void deleteAnswerEvaluationById(String evaluationId);

    void deleteEvaluationsByAnswerId(String answerId);
}