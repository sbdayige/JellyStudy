package org.example.qaservice.repository;


import org.bson.types.ObjectId;
import org.example.qacommon.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface QuestionRepository extends MongoRepository<Question, ObjectId> {
    // 分页查询
    Page<Question> findAll(Pageable pageable);

    // 分页搜索
    Page<Question> findByQuestionTextContaining(
            String keyword,
            Pageable pageable
    );

    @Query("{ 'hot_score' : { $gt: ?0 } }")
    Page<Question> findHotQuestionsByScore(Double minScore, Pageable pageable);

    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Question> findByHotScoreGreaterThanOrderByHotScoreDesc(
            Double minScore, Pageable pageable);
}

