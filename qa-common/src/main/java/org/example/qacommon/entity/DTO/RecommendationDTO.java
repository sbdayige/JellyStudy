// filepath: c:\Users\guoshenshen\Desktop\study\yyjg\class8\code\qa-service\src\main\java\org\example\qaservice\entity\DTO\RecommendationDTO.java
package org.example.qacommon.entity.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecommendationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private QuestionResponseDTO newestQuestion; // 修改为 DTO 类型
    private QuestionResponseDTO hottestQuestion; // 修改为 DTO 类型
    private QuestionResponseDTO mostAnsweredQuestion; // 修改为 DTO 类型 (如果需要)
    private String type; // 推荐类型
}