package org.example.qacommon.entity.DTO;

import lombok.Data;
import org.example.kgcommon.entity.KnowledgePoint;
import java.io.Serializable;
import java.util.List;

@Data
public class AnswerDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private AnswerResponseDTO answer;
    private List<CommentResponseDTO> comments;
    private CommentResponseDTO topComment;
    private int commentCount;
    private List<KnowledgePoint> knowledgePoints;
}