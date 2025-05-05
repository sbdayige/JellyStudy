package org.example.qaservice.service.Impl;

import com.mongodb.client.result.UpdateResult;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.example.aiservice.service.AiEvaluationService;
import org.example.aiservice.service.AiEvaluationServiceForAw;
import org.example.qacommon.entity.Answer;
import org.example.qacommon.entity.DTO.*;
import org.example.qacommon.entity.DeletionResult;
import org.example.qacommon.entity.Question;
import org.example.qacommon.entity.User;
import org.example.qaservice.repository.*;
import org.example.qaservice.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@DubboService
public class QuestionServiceImpl implements QuestionService {

    @DubboReference
    private AiEvaluationService aiEvaluationService; // 用于问题评估
    @DubboReference
    private AiEvaluationServiceForAw aiEvaluationServiceForAw; // 用于回答评估

    private final QuestionRepository repository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final UserServiceImpl userServiceImpl;
    private final HotScoreServiceImpl hotScoreServiceImpl;
    private final AnswerServiceImpl answerServiceImpl; // 注意：循环依赖风险，确保设计合理
    private final CommentServiceImpl commentServiceImpl; // 注意：循环依赖风险，确保设计合理
    private final MongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(QuestionServiceImpl.class);

    // 内部方法，获取实体
    @Override
    public Question findQuestionEntityById(String questionId) {
        try {
            ObjectId objectId = new ObjectId(questionId);
            return repository.findById(objectId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Question with id " + questionId + " not found"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid question ID format: " + questionId);
        }
    }

    // 查询方法 - 返回 DTO
    @Override
    public QuestionResponseDTO getQuestionById(String questionId) {
        Question question = findQuestionEntityById(questionId);
        return convertToResponseDTO(question);
    }

    // 新建问题 - 接受 DTO, 返回 DTO
    @Override
    public QuestionResponseDTO createQuestion(QuestionCreateDTO dto) {
        // 验证用户存在
        userServiceImpl.getUserById(dto.getUserId());

        Question question = convertToEntity(dto);
        question.setHotScore(0.0); // 初始化热度

        // 首次保存以获取ID
        Question saved = repository.save(question);

        // 生成路径并更新
        String newPath = String.format("/questions/%s", saved.getId().toHexString());
        saved.setPath(newPath);

        Question updatedQuestion = repository.save(saved);

        try {
            // 使用默认模型 (modelVersion 为 null)
            aiEvaluationService.evaluateQuestion(
                    updatedQuestion.getId().toHexString(),
                    updatedQuestion.getQuestionText(),
                    null // 使用默认模型
            );
            log.info("Successfully triggered AI evaluation for new question: {}",
                    updatedQuestion.getId().toHexString());
        } catch (Exception e) {
            // 记录 AI 评估失败的日志，但不影响问题创建流程
            log.error("Failed to trigger AI evaluation for new question {}: {}", updatedQuestion.getId().toHexString(),
                    e.getMessage(), e);
            // 这里可以选择不抛出异常，让问题创建成功
        }

        return convertToResponseDTO(updatedQuestion);
    }

    // 删除问题
    @Override
    @Transactional
    public DeletionResult deleteQuestion(ObjectId questionId) {
        String questionIdStr = questionId.toString();
        String questionPath = "/questions/" + questionIdStr;

        // !! 确保先调用 AI 删除 !!
        try {
            aiEvaluationService.deleteEvaluationsByQuestionId(questionIdStr);
            log.info("Deleted AI evaluations for questionId: {}", questionIdStr);
        } catch (Exception e) {
            log.error("Failed to delete AI evaluations for question {}: {}", questionIdStr, e.getMessage(), e);
            // 根据业务决定是否继续删除，或者抛出异常回滚
            // throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            // "删除问题AI评估失败", e);
        }

        // 获取所有关联回答
        List<Answer> answers = answerRepository.findByPathStartingWith(questionPath);
        int answersDeleted = answers.size();

        int commentsDeleted = 0;
        // int knowledgeDisassociations = 0; // 移除或调整知识点解关联逻辑

        for (Answer answer : answers) {
            String answerIdStr = answer.getId().toHexString();
            // 删除回答关联的评论
            String commentPathPrefix = answer.getPath() + "/comments/";
            long deletedComments = commentRepository.deleteByPathStartingWith(commentPathPrefix);
            commentsDeleted += (int) deletedComments;

            try {
                aiEvaluationServiceForAw.deleteEvaluationsByAnswerId(answerIdStr);
                log.info("Deleted AI evaluations for answerId: {}", answerIdStr);
            } catch (Exception e) {
                log.error("Failed to delete AI evaluations for answer {}: {}", answerIdStr, e.getMessage(), e);
            }

            answerRepository.delete(answer);
        }

        // 删除问题本身
        repository.deleteById(questionId);

        // 返回删除结果，移除 knowledgeDisassociations
        return new DeletionResult(
                "删除成功",
                answersDeleted,
                commentsDeleted,
                0 // knowledgeDisassociations 设为 0 或移除
        );
    }

    @Override
    public Page<QuestionResponseDTO> getNewestQuestions(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Question> questionPage = repository.findAll(pageRequest);
        List<QuestionResponseDTO> dtoList = questionPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageRequest, questionPage.getTotalElements());
    }

    @Override
    public QuestionResponseDTO getHottestQuestion() {
        Page<Question> questions = repository.findHotQuestionsByScore(
                0.0,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "hotScore")));

        Question hottestQuestion = questions.getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "当前没有热点问题"));
        return convertToResponseDTO(hottestQuestion);
    }

    @Override
    public void incrementUpvotes(String questionId, String userId) {
        try {
            Query query = new Query(Criteria.where("_id").is(new ObjectId(questionId)));
            Update update = new Update().inc("upvotes", 1);

            UpdateResult result = mongoTemplate.updateFirst(query, update, Question.class);

            if (result.getMatchedCount() == 0) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "问题不存在: " + questionId);
            }
            Query userQuery = Query.query(Criteria.where("_id").is(new ObjectId(userId)));
            Update userUpdate = new Update().addToSet("question_upvotes", questionId);
            mongoTemplate.updateFirst(userQuery, userUpdate, User.class);
            hotScoreServiceImpl.incrementHotScore(questionId, 1.0);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的问题ID格式");
        }
    }

    @Override
    public void decrementUpvotes(String questionId, String userId) {
        try {
            // 减少问题点赞数
            Query questionQuery = new Query(Criteria.where("_id").is(new ObjectId(questionId)));
            Update questionUpdate = new Update().inc("upvotes", -1);
            mongoTemplate.updateFirst(questionQuery, questionUpdate, Question.class);

            // 移除用户点赞记录
            Query userQuery = new Query(Criteria.where("_id").is(new ObjectId(userId)));
            Update userUpdate = new Update().pull("question_upvotes", questionId);
            mongoTemplate.updateFirst(userQuery, userUpdate, User.class);

            // 热度值计算
            hotScoreServiceImpl.incrementHotScore(questionId, -1.0);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的ID格式");
        }
    }

    @Override
    public long getTotalQuestions() {
        return repository.count();
    }

    @Override
    public QuestionDetailsDTO getQuestionDetails(String questionId) {
        // 1. 获取基础问题信息 DTO
        QuestionResponseDTO questionDTO = getQuestionById(questionId);

        // 2. 获取所有回答 DTO
        List<AnswerResponseDTO> answerDTOs = answerServiceImpl.getAnswersByQuestionId(questionId);

        // 3. 构建详细DTO
        QuestionDetailsDTO dto = new QuestionDetailsDTO();
        dto.setQuestion(questionDTO); // 设置 DTO
        dto.setTotalAnswers(answerDTOs.size());

        // 4. 获取置顶回答 DTO
        try {
            dto.setTopAnswer(answerServiceImpl.getTopAnswerByUpvotes(questionId));
        } catch (ResponseStatusException e) {
            // 无置顶回答时忽略
        }

        // 5. 构建回答详情列表
        List<AnswerDetailsDTO> answerDetails = answerDTOs.stream()
                .map(answerDTO -> {
                    AnswerDetailsDTO detail = new AnswerDetailsDTO();
                    detail.setAnswer(answerDTO);

                    // 修改为获取DTO列表
                    List<CommentResponseDTO> comments = commentServiceImpl.getCommentsByAnswer(answerDTO.getId());
                    detail.setComments(comments);
                    detail.setCommentCount(comments.size());

                    // 修改为获取DTO
                    try {
                        detail.setTopComment(commentServiceImpl.getTopCommentByAnswer(
                                questionId,
                                answerDTO.getId()));
                    } catch (ResponseStatusException e) {
                        // 无置顶评论时忽略
                    }
                    return detail;
                })
                .collect(Collectors.toList());

        dto.setAnswers(answerDetails);
        return dto;
    }

    // --- Helper Methods ---
    private Question convertToEntity(QuestionCreateDTO dto) {
        Question question = new Question();
        question.setQuestionText(dto.getQuestionText());
        question.setUser_id(dto.getUserId());
        // 其他字段（如 upvotes, hotScore, path, createdAt, updatedAt）将在保存或后续逻辑中设置
        return question;
    }

    private QuestionResponseDTO convertToResponseDTO(Question entity) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(entity.getId().toHexString());
        dto.setQuestionText(entity.getQuestionText());
        dto.setHotScore(entity.getHotScore());
        dto.setUpvotes(entity.getUpvotes());
        dto.setUserId(entity.getUser_id());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setPath(entity.getPath());
        return dto;
    }
}