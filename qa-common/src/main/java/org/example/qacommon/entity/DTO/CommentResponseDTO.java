package org.example.qacommon.entity.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String content;
    private int upvotes;
    private Date createdAt;
    private String path;
    private String userId; // 原user_id字段
}
