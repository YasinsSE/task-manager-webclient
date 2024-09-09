package com.project.webclient_service.service;

import com.project.webclient_service.dto.TaskRequestDTO;
import com.project.webclient_service.dto.TaskResponseDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public interface TaskService {
    Mono<List<TaskResponseDTO>> getAllTasks();
    Mono<TaskResponseDTO> getTaskById(Long taskId);
    Mono<TaskResponseDTO> createTask(TaskRequestDTO taskRequestDTO);
    Mono<TaskResponseDTO> updateTask(TaskRequestDTO taskRequestDTO);
    Mono<Void> deleteTask(Long taskId);
}