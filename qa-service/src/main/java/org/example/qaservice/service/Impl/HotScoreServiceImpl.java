package org.example.qaservice.service.Impl;

import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;

import org.example.qaservice.entity.Question;
import org.example.qaservice.service.HotScoreService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@DubboService
public class HotScoreServiceImpl implements HotScoreService {
    private final MongoTemplate mongoTemplate;

    public HotScoreServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void incrementHotScore(String questionId, double delta) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(questionId)));
        Update update = new Update().inc("hotScore", delta);
        mongoTemplate.updateFirst(query, update, Question.class);
    }
}
