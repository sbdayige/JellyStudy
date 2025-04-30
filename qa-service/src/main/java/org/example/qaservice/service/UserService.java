package org.example.qaservice.service;


import org.example.qaservice.entity.DTO.UserCreateDTO;
import org.example.qaservice.entity.DTO.UserResponseDTO;
import org.example.qaservice.entity.User;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO userDTO);
    UserResponseDTO getUserById(String userId);
}