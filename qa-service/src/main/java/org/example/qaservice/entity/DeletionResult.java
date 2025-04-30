package org.example.qaservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private int deletedAnswers;
    private int deletedComments;
    private int disassociatedKnowledgePoints;
}