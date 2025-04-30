package org.example.qaservice.entity.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "问题内容不能为空")
    @Size(min = 10, max = 200, message = "问题内容长度需在10-200字符之间")
    private String questionText;

    @NotBlank(message = "用户ID不能为空")
    private String userId;
}