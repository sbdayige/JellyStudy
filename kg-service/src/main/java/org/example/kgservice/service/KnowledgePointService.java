package org.example.kgservice.service;

import jakarta.validation.Valid;
import org.example.kgcommon.entity.DTO.CreateKnowledgePointDTO;
import org.example.kgcommon.entity.DTO.KnowledgePointResponseDTO;
import org.springframework.web.server.ResponseStatusException;


public interface KnowledgePointService {
    KnowledgePointResponseDTO createKnowledgePoint(@Valid CreateKnowledgePointDTO dto);
    KnowledgePointResponseDTO getKnowledgePointById(String id) throws ResponseStatusException;
}