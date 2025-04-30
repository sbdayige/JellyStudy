package org.example.qaservice.entity;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Field("_id")
    private ObjectId id;

    @NotNull
    @Field("role")
    private String role;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50个字符")
    @Field("name")
    private String name;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度8-20位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "密码必须包含字母和数字")
    @Field("password")
    private String password;

    @CreatedDate
    @Field("created_at")
    private Date createdAt;

    @Field("question_upvotes")
    private List<String> questionUpvotes = new ArrayList<>();

    @Field("answer_upvotes")
    private List<String> answerUpvotes = new ArrayList<>();

    @Field("comment_upvotes")
    private List<String> commentUpvotes = new ArrayList<>();
}
