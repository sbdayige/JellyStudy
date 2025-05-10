package org.example.qacontroller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.qacommon.entity.DTO.UserCreateDTO;
import org.example.qacommon.entity.DTO.UserResponseDTO;
import org.example.qaservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API")
public class UserController {

    @DubboReference
    private UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建用户", description = "创建新用户并返回用户信息")
    @ApiResponse(responseCode = "201", description = "用户创建成功", content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    public UserResponseDTO createUser(@Valid @RequestBody UserCreateDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户信息", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    public UserResponseDTO getUser(@PathVariable String userId) {
        return userService.getUserById(userId);
    }
}