package com.project.webclient_service.controller;

import com.project.webclient_service.dto.UserRequestDTO;
import com.project.webclient_service.dto.UserResponseDTO;
import com.project.webclient_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/web-client")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("get-users")
    public ResponseEntity<Mono<List<UserResponseDTO>>> getAllUsers() {
        log.info("Received request to get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("get-user/{id}")
    public ResponseEntity<Mono<UserResponseDTO>> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @PostMapping("create-user")
    public ResponseEntity<Mono<UserResponseDTO>> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        log.info("Received request to create user with email: {}", userRequestDTO.getUserEmail());
        return ResponseEntity.ok(userService.createUser(userRequestDTO));
    }


    @PutMapping("update-user")
    public ResponseEntity<Mono<UserResponseDTO>> updateUser(@RequestBody UserRequestDTO userRequestDTO) {
        log.info("Received request to update user with ID: {} and email: {}", userRequestDTO.getUserId(), userRequestDTO.getUserEmail());
        return ResponseEntity.ok(userService.updateUser(userRequestDTO));
    }


    @DeleteMapping("delete-user/{id}")
    public ResponseEntity<Mono<Void>> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete user with ID: {}", id);
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}