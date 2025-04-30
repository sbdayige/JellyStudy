package org.example.aiservice.repository;

import org.example.aiservice.entity.AIForAw;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AIForAwRepository extends MongoRepository<AIForAw, String> {
    List<AIForAw> findByAnswerId(String answerId);

    @Query("{'questionId': ?0}")
    List<AIForAw> findByQuestionId(String questionId);

    void deleteByAnswerId(String answerId);
}