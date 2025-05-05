package org.example.kgservice.repository;

import org.example.kgcommon.entity.KnowledgePoint;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KnowledgePointRepository extends MongoRepository<KnowledgePoint, String> {
}