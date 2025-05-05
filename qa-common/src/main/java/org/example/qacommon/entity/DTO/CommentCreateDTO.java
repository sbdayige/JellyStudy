package org.example.qacommon.entity.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @NotBlank(message = "用户ID不能为空")
    private String userId; // 对应原Comment实体中的user_id

    private Date createdAt;
}
