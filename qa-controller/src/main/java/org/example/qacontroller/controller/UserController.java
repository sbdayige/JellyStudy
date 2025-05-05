package org.example.qacontroller.controller;



import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.qacommon.entity.DTO.UserCreateDTO;
import org.example.qacommon.entity.DTO.UserResponseDTO;
import org.example.qaservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @DubboReference
    private UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserCreateDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @GetMapping("/{userId}")
    public UserResponseDTO getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }
}