package org.example.qaservice.entity.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UserResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    private List<String> questionUpvotes;
    private List<String> answerUpvotes;
    private List<String> commentUpvotes;
}