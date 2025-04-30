package org.example.qaservice.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Field("_id")  // 明确映射MongoDB的主键字段
    private ObjectId id;  // 类型改为ObjectId

    @NotBlank
    private String content;

    @Min(0)
    private int upvotes;

    @Pattern(regexp = "^/questions/[0-9a-f]{24}/answers/[0-9a-f]{24}/comments/[0-9a-f]{24}$")
    private String path;

    @Field("user_id")
    @NotNull
    private String user_id;  // 保持String类型（根据数据中的字符串值）

    @CreatedDate
    private Date createdAt;  // 保留审计字段（由Spring自动填充）
}

