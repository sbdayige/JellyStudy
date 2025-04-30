package org.example.qaservice.repository;

import org.bson.types.ObjectId;
import org.example.qaservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
}