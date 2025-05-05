package org.example.qaservice.service.Impl;

import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.example.aiservice.service.AiEvaluationServiceForAw;
import org.example.kgservice.repository.KnowledgePointRepository;
import org.example.qacommon.entity.Answer;
import org.example.qacommon.entity.DTO.AnswerCreateDTO;
import org.example.qacommon.entity.DTO.AnswerResponseDTO;
import org.example.qacommon.entity.DeletionResult;
import org.example.qacommon.entity.Question;
import org.example.qacommon.entity.User;
import org.example.qaservice.repository.*;
import org.example.qaservice.service.AnswerService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@RequiredArgsConstructor
@Slf4j
public class AnswerServiceImpl implements AnswerService {

    private final HotScoreServiceImpl hotScoreServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;
    private final CommentRepository commentRepository;
    private final KnowledgePointRepository knowledgePointRepository;

    @DubboReference
    private AiEvaluationServiceForAw aiEvaluationServiceForAw;

    @Override
    public AnswerResponseDTO createAnswer(String questionId, AnswerCreateDTO dto) {
        // 验证用户存在
        userServiceImpl.getUserById(dto.getUserId());

        ObjectId questionObjectId = new ObjectId(questionId);

        Question question = questionRepository.findById(questionObjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "问题不存在"));

        // 转换DTO到实体
        Answer answer = convertToEntity(dto);

        // 处理知识点验证
        if (answer.getKnowledgePoints() != null) {
            answer.getKnowledgePoints().forEach(kpId -> {
                if (!knowledgePointRepository.existsById(kpId)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "知识点 " + kpId + " 不存在");
                }
            });
        }

        // 保存并生成路径
        Answer saved = answerRepository.save(answer);
        String newPath = String.format("/questions/%s/answers/%s",
                questionId,
                saved.getId().toHexString());
        saved.setPath(newPath);

        Answer updated = answerRepository.save(saved);
        hotScoreServiceImpl.incrementHotScore(questionId, 15.0);

        try {
            // 使用默认模型 (modelVersion 为 null)
            aiEvaluationServiceForAw.evaluateAnswer(
                    questionId,
                    updated.getId().toHexString(), // 改为 updated
                    question.getQuestionText(), // 获取问题文本
                    updated.getContent(), // 改为 updated
                    null // 使用默认模型
            );
            log.info("Successfully triggered AI evaluation for new answer: {}", updated.getId().toHexString()); // 改为
                                                                                                                // updated
        } catch (Exception e) {
            // 记录 AI 评估失败的日志，但不影响回答创建流程
            log.error("Failed to trigger AI evaluation for new answer {}: {}", updated.getId().toHexString(),
                    e.getMessage(), e); // 改为 updated
            // 这里可以选择不抛出异常，让回答创建成功
        }

        return convertToResponseDTO(updated);
    }

    private Answer convertToEntity(AnswerCreateDTO dto) {
        Answer answer = new Answer();
        answer.setContent(dto.getContent());
        answer.setUser_id(dto.getUserId());
        answer.setKnowledgePoints(dto.getKnowledgePoints());
        return answer;
    }

    private AnswerResponseDTO convertToResponseDTO(Answer answer) {
        AnswerResponseDTO dto = new AnswerResponseDTO();
        dto.setId(answer.getId().toHexString());
        dto.setContent(answer.getContent());
        dto.setUserId(answer.getUser_id());
        dto.setUpvotes(answer.getUpvotes());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setUpdatedAt(answer.getUpdatedAt());
        dto.setPath(answer.getPath());
        dto.setKnowledgePoints(answer.getKnowledgePoints());
        return dto;
    }

    @Override
    @Transactional
    public DeletionResult deleteAnswer(String questionId, String answerId) {
        try {
            ObjectId answerObjectId = new ObjectId(answerId);
            Answer answer = answerRepository.findById(answerObjectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "回答不存在"));

            // 验证路径归属
            if (!answer.getPath().startsWith("/questions/" + questionId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "回答不属于该问题");
            }

            double hotScoreDelta = -15.0 - answer.getUpvotes();

            // 新增知识点解关联逻辑
            int disassociatedCount = 0;

            // 删除关联评论
            String commentPathPrefix = answer.getPath() + "/comments/";
            int commentsDeleted = (int) commentRepository.deleteByPathStartingWith(commentPathPrefix);

            // 删除回答
            answerRepository.delete(answer);

            try {
                aiEvaluationServiceForAw.deleteEvaluationsByAnswerId(answerId);
                log.info("Deleted AI evaluations for answerId: {}", answerId);
            } catch (Exception e) {
                log.error("Failed to delete AI evaluations for answer {}: {}", answerId, e.getMessage(), e);
            }
            hotScoreServiceImpl.incrementHotScore(questionId, hotScoreDelta);

            return new DeletionResult(
                    "删除成功",
                    1,
                    commentsDeleted,
                    disassociatedCount // 返回解除关联数
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的回答ID格式");
        }
    }

    @Override
    public List<AnswerResponseDTO> getAnswersByQuestionId(String questionId) {
        // 生成正则表达式模式
        String regexPattern = "^/questions/" + questionId + "/answers/.*";

        // 调用正则查询
        return answerRepository.findByPathRegex(regexPattern)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AnswerResponseDTO getAnswerById(String questionId, String answerId) {
        try {
            ObjectId answerObjectId = new ObjectId(answerId);

            // 验证回答存在
            Answer answer = answerRepository.findById(answerObjectId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "回答不存在"));

            // 验证回答归属
            String expectedPathPrefix = "/questions/" + questionId;
            if (!answer.getPath().startsWith(expectedPathPrefix)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "该回答不属于当前问题");
            }

            return convertToResponseDTO(answer);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的回答ID格式");
        }
    }

    @Override
    public void incrementUpvotes(String questionId, String answerId, String userId) {
        try {
            ObjectId answerObjectId = new ObjectId(answerId);

            // 验证回答存在性
            Answer answer = answerRepository.findById(answerObjectId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "回答不存在"));

            // 验证回答归属
            String expectedPathPrefix = "/questions/" + questionId;
            if (!answer.getPath().startsWith(expectedPathPrefix)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "该回答不属于当前问题");
            }

            // 使用原子操作更新点赞数
            Query query = new Query(Criteria.where("_id").is(answerObjectId));
            Update update = new Update().inc("upvotes", 1);
            UpdateResult result = mongoTemplate.updateFirst(query, update, Answer.class);
            Query userQuery = Query.query(Criteria.where("_id").is(new ObjectId(userId)));
            Update userUpdate = new Update().addToSet("answer_upvotes", answerId);
            mongoTemplate.updateFirst(userQuery, userUpdate, User.class);

            if (result.getMatchedCount() == 0) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "点赞更新失败");
            }

            hotScoreServiceImpl.incrementHotScore(questionId, 1.0);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的回答ID格式");
        }
    }

    @Override
    public void decrementUpvotes(String questionId, String answerId, String userId) {
        try {
            // 减少回答点赞数
            Query answerQuery = new Query(Criteria.where("_id").is(new ObjectId(answerId)));
            Update answerUpdate = new Update().inc("upvotes", -1);
            mongoTemplate.updateFirst(answerQuery, answerUpdate, Answer.class);

            // 移除用户点赞记录
            Query userQuery = new Query(Criteria.where("_id").is(new ObjectId(userId)));
            Update userUpdate = new Update().pull("answer_upvotes", answerId);
            mongoTemplate.updateFirst(userQuery, userUpdate, User.class);

            // 热度值计算
            hotScoreServiceImpl.incrementHotScore(questionId, -1.0);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的ID格式");
        }
    }

    @Override
    public AnswerResponseDTO getTopAnswerByUpvotes(String questionId) {
        // 构建查询条件
        String pathRegex = "^/questions/" + questionId + "/answers/.*";
        Query query = new Query(Criteria.where("path").regex(pathRegex))
                .with(Sort.by(Sort.Direction.DESC, "upvotes"))
                .limit(1);

        List<Answer> answers = mongoTemplate.find(query, Answer.class);

        if (answers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "该问题下暂无回答");
        }
        return convertToResponseDTO(answers.get(0));
    }

    @Override
    public List<AnswerResponseDTO> getAnswersByKnowledgePoint(String knowledgeId) {
        return answerRepository.findByKnowledgePointsContaining(knowledgeId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
}
