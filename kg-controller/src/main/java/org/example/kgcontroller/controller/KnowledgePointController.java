package org.example.kgcontroller.controller;

import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.kgservice.entity.DTO.CreateKnowledgePointDTO;
import org.example.kgservice.entity.DTO.KnowledgePointResponseDTO;
import org.example.kgservice.entity.ErrorResponse;
import org.example.kgservice.entity.KnowledgePoint;
import org.example.kgservice.service.Impl.KnowledgePointServiceImpl;
import org.example.kgservice.service.KnowledgePointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/knowledge-points")
public class KnowledgePointController {
    @DubboReference
    private KnowledgePointService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgePointResponseDTO createKnowledgePoint(
            @RequestBody @Valid CreateKnowledgePointDTO dto) {
        return service.createKnowledgePoint(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getKnowledgePointById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.getKnowledgePointById(id));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(new ErrorResponse(
                            (HttpStatus) e.getStatusCode(),
                            e.getReason(),
                            "/api/knowledge-points/" + id
                    ));
        }
    }
}
