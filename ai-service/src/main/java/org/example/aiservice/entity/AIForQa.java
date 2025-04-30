package org.example.aiservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ai_qa_evaluations")
@Data
public class AIForQa implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private String questionId;
    @JsonProperty("knowledge_points") // 关联问题ID
    private List<String> knowledgePoints; // 提取的知识点列表
    @JsonProperty("difficulty_level")
    private String difficultyLevel; // 难度等级（EASY/MEDIUM/HARD）
    @CreatedDate
    @Field("evaluated_at")
    private LocalDateTime evaluatedAt;// 评估时间
    private String modelVersion; // 使用的大模型版本
    @JsonProperty("confidence_score")
    private Double confidenceScore; // 模型置信度分数
}