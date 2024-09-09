package com.project.webclient_service.service;

import com.project.webclient_service.dto.UserRequestDTO;
import com.project.webclient_service.dto.UserResponseDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public interface UserService {
    Mono<List<UserResponseDTO>> getAllUsers();
    Mono<UserResponseDTO> getUserById(Long userId);
    Mono<UserResponseDTO> createUser(UserRequestDTO userRequestDTO);
    Mono<UserResponseDTO> updateUser(UserRequestDTO userRequestDTO);
    Mono<Void> deleteUser(Long userId);
}
