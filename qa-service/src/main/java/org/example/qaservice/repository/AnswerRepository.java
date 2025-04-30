package org.example.qaservice.repository;

import org.bson.types.ObjectId;
import org.example.qaservice.entity.Answer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AnswerRepository extends MongoRepository<Answer, ObjectId> {
    @Query("{ 'path' : { $regex: ?0 } }")
    List<Answer> findByPathRegex(String regexPattern);

    List<Answer> findByPathStartingWith(String pathPrefix);

    List<Answer> findByKnowledgePointsContaining(String knowledgePointId);

}