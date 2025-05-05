package org.example.kgcommon.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "knowledge_points")
public class KnowledgePoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Field("_id")  // 显式映射MongoDB主键字段
    @Pattern(regexp = "^k\\d+$")
    private String id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Field("title")
    private String title;

    @NotNull
    @Field("category")
    private String category;

    @NotBlank
    @Size(min = 10, max = 500)
    @Field("description")
    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
