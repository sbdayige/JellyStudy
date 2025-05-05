package org.example.qaservice.repository;

import org.bson.types.ObjectId;
import org.example.qacommon.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {
}