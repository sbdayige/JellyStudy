package org.example.qaservice.service.Impl;

import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.example.qacommon.entity.DTO.UserCreateDTO;
import org.example.qacommon.entity.DTO.UserResponseDTO;
import org.example.qacommon.entity.User;
import org.example.qaservice.repository.UserRepository;
import org.example.qaservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@DubboService
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDTO createUser(UserCreateDTO userDTO) {
        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserById(String userId) {
        User user = userRepository.findById(new ObjectId(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertToResponseDTO(user);
    }

    private User convertToEntity(UserCreateDTO dto) {
        User user = new User();
        user.setName(dto.getUsername());
        user.setPassword(dto.getPassword());
        return user;
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId().toHexString());
        dto.setUsername(user.getName());
        return dto;
    }
}
