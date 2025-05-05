package org.example.qacommon.entity.DTO;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class QuestionResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String questionText;
    private Double hotScore;
    private int upvotes;
    private String userId;
    private Date createdAt;
    private Date updatedAt;
    private String path;
}