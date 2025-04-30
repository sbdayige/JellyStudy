package org.example.qaservice.service;

public interface HotScoreService {

    void incrementHotScore(String questionId, double delta);

}