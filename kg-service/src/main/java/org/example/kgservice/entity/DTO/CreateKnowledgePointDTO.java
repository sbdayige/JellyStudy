package org.example.kgservice.entity.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateKnowledgePointDTO {
    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 50, message = "标题长度需在2-50字符之间")
    private String title;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 500, message = "描述长度需在10-500字符之间")
    private String description;
}
