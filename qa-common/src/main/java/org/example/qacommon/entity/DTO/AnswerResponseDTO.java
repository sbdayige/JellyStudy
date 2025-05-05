package org.example.qacommon.entity.DTO;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class AnswerResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String content;
    private List<String> knowledgePoints;
    private String userId;
    private int upvotes;
    private Date createdAt;
    private Date updatedAt;
    private String path;
}