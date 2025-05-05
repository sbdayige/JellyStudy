package org.example.qacommon.entity;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_evaluations")
public class AiEvaluation {
    @Id
    @Field("_id")
    private ObjectId id; // 改为MongoDB的ObjectId类型

    @Field("ai_score")
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double aiScore;

    @Field("ai_analysis")
    @NotBlank
    @Size(min = 10, max = 500)
    private String aiAnalysis;

    @Field("knowledge_coverage")
    @Valid
    private List<KnowledgeCoverage> knowledgeCoverage;

    @NotBlank
    @Pattern(regexp = "^/questions/[0-9a-f]{24}/answers/[0-9a-f]{24}/ai_eval$")
    private String path;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeCoverage {
        @Field("knowledge_id")
        @NotBlank
        private String knowledgeId;

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private Double coverage;
    }
}