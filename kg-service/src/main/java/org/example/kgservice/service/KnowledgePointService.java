package org.example.kgservice.service;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.example.kgservice.entity.DTO.CreateKnowledgePointDTO;
import org.example.kgservice.entity.DTO.KnowledgePointResponseDTO;
import org.example.kgservice.entity.KnowledgePoint;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;


public interface KnowledgePointService {
    KnowledgePointResponseDTO createKnowledgePoint(@Valid CreateKnowledgePointDTO dto);
    KnowledgePointResponseDTO getKnowledgePointById(String id) throws ResponseStatusException;
}