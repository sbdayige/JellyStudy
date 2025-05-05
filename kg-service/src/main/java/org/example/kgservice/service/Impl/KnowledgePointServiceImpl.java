package org.example.kgservice.service.Impl;

import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboService;
import org.example.kgcommon.entity.DTO.CreateKnowledgePointDTO;
import org.example.kgcommon.entity.DTO.KnowledgePointResponseDTO;
import org.example.kgcommon.entity.KnowledgePoint;
import org.example.kgservice.repository.KnowledgePointRepository;
import org.example.kgservice.service.KnowledgePointService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@DubboService
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgePointRepository repository;
    private final MongoTemplate mongoTemplate;

    public KnowledgePointServiceImpl(KnowledgePointRepository repository,
                                     MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public KnowledgePointResponseDTO createKnowledgePoint(@Valid CreateKnowledgePointDTO dto) {
        String newId = generateKnowledgePointId();

        KnowledgePoint entity = new KnowledgePoint();
        entity.setId(newId);
        entity.setTitle(dto.getTitle());
        entity.setCategory(dto.getCategory());
        entity.setDescription(dto.getDescription());

        // 保留原有校验
        if (!newId.matches("^k\\d+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "知识点ID格式必须为k开头后接数字（如k001）");
        }
        if (repository.existsById(newId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "知识点ID已存在");
        }

        KnowledgePoint saved = repository.save(entity);
        return convertToResponseDTO(saved);
    }

    @Override
    public KnowledgePointResponseDTO getKnowledgePointById(String id) {
        KnowledgePoint entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到知识点"));
        return convertToResponseDTO(entity);
    }

    private String generateKnowledgePointId() {
        // 实现ID生成逻辑（示例：k001格式）
        long count = repository.count() + 1;
        return String.format("k%03d", count);
    }

    private KnowledgePointResponseDTO convertToResponseDTO(KnowledgePoint entity) {
        KnowledgePointResponseDTO dto = new KnowledgePointResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}

