// filepath: c:\Users\guoshenshen\Desktop\study\yyjg\class8\code\qa-service\src\main\java\org\example\qaservice\entity\DTO\QuestionDetailsDTO.java
package org.example.qacommon.entity.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private QuestionResponseDTO question; // 修改为 DTO 类型
    private AnswerResponseDTO topAnswer;
    private List<AnswerDetailsDTO> answers;
    private int totalAnswers;
}