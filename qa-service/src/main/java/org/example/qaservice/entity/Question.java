package org.example.qaservice.entity;


import jakarta.validation.constraints.DecimalMin;
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
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Field("_id")
    private ObjectId id;

    @NotBlank
    @Field("question_text")
    @Size(min = 10, max = 200)
    private String questionText;

    @DecimalMin("0.0")
    @Field("hot_score")
    private Double hotScore;

    @Field("upvotes")
    private int upvotes;

    @Field("user_id")
    @NotNull
    private String user_id;

    @CreatedDate
    @Field("created_at")
    private Date createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Date updatedAt;

    @NotBlank
    @Pattern(regexp = "^/questions/[0-9a-f]{24}$")
    private String path;
}
