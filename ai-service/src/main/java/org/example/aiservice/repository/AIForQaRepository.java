package org.example.aiservice.repository;

import org.example.aicommon.entity.AIForQa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AIForQaRepository extends MongoRepository<AIForQa, String> {
    Optional<AIForQa> findByQuestionId(String questionId);

    @Query("{'knowledgePoints': ?0}")
    List<AIForQa> findByKnowledgePoint(String pointId);

    void deleteByQuestionId(String questionId);
}
