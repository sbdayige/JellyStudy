package org.example.qaservice.repository;

import org.bson.types.ObjectId;
import org.example.qacommon.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {
    List<Comment> findByPathStartingWith(String pathPrefix);

    @Query(value = "{'path' : {$regex: ?0}}", delete = true)
    long deleteByPathStartingWith(String pathPrefix);
}