// AnswerCreateDTO.java
package org.example.qaservice.entity.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class AnswerCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "回答内容不能为空")
    private String content;

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    private List<String> knowledgePoints;
}