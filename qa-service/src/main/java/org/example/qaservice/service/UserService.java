package org.example.qaservice.service;


import org.example.qacommon.entity.DTO.UserCreateDTO;
import org.example.qacommon.entity.DTO.UserResponseDTO;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO userDTO);
    UserResponseDTO getUserById(String userId);
}