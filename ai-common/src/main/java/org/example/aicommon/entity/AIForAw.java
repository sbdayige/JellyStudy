// filepath: c:\Users\guoshenshen\Desktop\study\yyjg\class8\code\ai-service\src\main\java\org\example\aiservice\entity\AIForAw.java
package org.example.aicommon.entity;

import com.fasterxml.jackson.annotation.JsonProperty; // 导入 JsonProperty
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // 导入 Field

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ai_aw_evaluations")
@Data
public class AIForAw implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private String answerId; // 关联回答ID

    @JsonProperty("quality_score") // 添加注解指定JSON字段名
    private Double qualityScore; // 质量评分（百分制）

    private List<String> feedbacks; // 改进建议列表 (假设JSON字段名也是 feedbacks)

    @JsonProperty("evaluation_summary") // 添加注解指定JSON字段名
    private String evaluationSummary; // 评估摘要

    @Field("evaluated_at") // 如果MongoDB字段名不同，也可用@Field
    private LocalDateTime evaluatedAt;// 评估时间

    private String modelVersion; // 使用的大模型版本
    private String questionId; // 冗余存储用于关联查询
}