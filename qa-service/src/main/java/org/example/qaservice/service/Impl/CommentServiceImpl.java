package org.example.qaservice.service.Impl;

import com.mongodb.client.result.UpdateResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.example.qacommon.entity.Answer;
import org.example.qacommon.entity.Comment;
import org.example.qacommon.entity.DTO.CommentCreateDTO;
import org.example.qacommon.entity.DTO.CommentResponseDTO;
import org.example.qacommon.entity.User;
import org.example.qaservice.repository.AnswerRepository;
import org.example.qaservice.repository.CommentRepository;
import org.example.qaservice.service.CommentService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@DubboService
public class CommentServiceImpl implements CommentService {

    private final HotScoreServiceImpl hotScoreServiceImpl;
    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final MongoTemplate mongoTemplate;

    private String extractQuestionId(String path) {
        return path.split("/")[2]; // /questions/{questionId}/...
    }

    @Override
    public CommentResponseDTO createComment(String answerId, CommentCreateDTO dto) {
        Answer answer = answerRepository.findById(new ObjectId(answerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "答案不存在"));

        // 转换DTO到实体
        Comment comment = convertToEntity(dto);
        comment.setPath("/questions/" + extractQuestionIdFromAnswerPath(answerId) + "/answers/" + answerId);

        // 保存并转换响应DTO
        Comment savedComment = commentRepository.save(comment);
        hotScoreServiceImpl.incrementHotScore(extractQuestionIdFromAnswerPath(answerId), 15.0);
        return convertToResponseDTO(savedComment);
    }

    @Override
    public List<CommentResponseDTO> getCommentsByAnswer(String answerId) {
        String pathPrefix = "/questions/%s/answers/%s/comments/".formatted(
                extractQuestionIdFromAnswerPath(answerId), answerId);
        return commentRepository.findByPathStartingWith(pathPrefix)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private String extractQuestionIdFromAnswerPath(String answerId) {
        try {
            Answer answer = answerRepository.findById(new ObjectId(answerId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "答案不存在"));

            String[] pathSegments = answer.getPath().split("/");

            if (pathSegments.length < 4
                    || !"questions".equals(pathSegments[1])
                    || !"answers".equals(pathSegments[3])) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "答案路径格式异常: " + answer.getPath());
            }
            return pathSegments[2];
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的答案ID格式: " + answerId);
        }
    }

    @Override
    public void deleteComment(String questionId, String answerId, String commentId) {
        try {
            ObjectId commentObjectId = new ObjectId(commentId);

            // 1. 验证评论存在
            Comment comment = commentRepository.findById(commentObjectId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "评论不存在"));

            double hotScoreDelta = -15.0 - comment.getUpvotes();
            // 2. 验证路径归属
            String expectedPathPrefix = String.format("/questions/%s/answers/%s",
                    questionId, answerId);

            if (!comment.getPath().startsWith(expectedPathPrefix)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "该评论不属于当前回答");
            }

            hotScoreServiceImpl.incrementHotScore(extractQuestionId(comment.getPath()), hotScoreDelta);

            // 3. 执行删除
            commentRepository.deleteById(commentObjectId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "无效的评论ID格式");
        }
    }

    @Override
    public void incrementUpvotes(String questionId, String answerId, String commentId, String userId) {
        try {
            ObjectId commentObjectId = new ObjectId(commentId);

            // 1. 验证评论存在
            Comment comment = commentRepository.findById(commentObjectId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "评论不存在"));

            // 2. 验证路径归属
            String expectedPath = String.format("/questions/%s/answers/%s/comments/%s",
                    questionId, answerId, commentId);

            if (!expectedPath.equals(comment.getPath())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "评论路径不匹配");
            }

            // 3. 使用原子操作更新点赞数
            Query query = new Query(Criteria.where("_id").is(commentObjectId));
            Update update = new Update().inc("upvotes", 1);
            UpdateResult result = mongoTemplate.updateFirst(query, update, Comment.class);

            Query userQuery = Query.query(Criteria.where("_id").is(new ObjectId(userId)));
            Update userUpdate = new Update().addToSet("comment_upvotes", commentId);
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
                    "无效的评论ID格式");
        }
    }

    @Override
    public void decrementUpvotes(String questionId, String answerId, String commentId, String userId) {
        try {
            // 减少评论点赞数
            Query commentQuery = new Query(Criteria.where("_id").is(new ObjectId(commentId)));
            Update commentUpdate = new Update().inc("upvotes", -1);
            mongoTemplate.updateFirst(commentQuery, commentUpdate, Comment.class);

            // 移除用户点赞记录
            Query userQuery = new Query(Criteria.where("_id").is(new ObjectId(userId)));
            Update userUpdate = new Update().pull("comment_upvotes", commentId);
            mongoTemplate.updateFirst(userQuery, userUpdate, User.class);

            // 热度值计算
            hotScoreServiceImpl.incrementHotScore(questionId, -1.0);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的ID格式");
        }
    }

    @Override
    public CommentResponseDTO getTopCommentByAnswer(String questionId, String answerId) {

        String pathPrefix = String.format("/questions/%s/answers/%s/comments/", questionId, answerId);

        Query query = new Query(Criteria.where("path").regex("^" + pathPrefix))
                .with(Sort.by(Sort.Direction.DESC, "upvotes"))
                .limit(1);

        return mongoTemplate.find(query, Comment.class)
                .stream()
                .findFirst()
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "该回答下暂无评论"));
    }

    private Comment convertToEntity(CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setUser_id(dto.getUserId());
        comment.setCreatedAt(new Date());
        return comment;
    }

    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId().toHexString());
        dto.setContent(comment.getContent());
        dto.setUpvotes(comment.getUpvotes());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setPath(comment.getPath());
        dto.setUserId(comment.getUser_id());
        return dto;
    }
}
