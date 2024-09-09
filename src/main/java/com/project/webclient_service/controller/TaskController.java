package com.project.webclient_service.controller;

import com.project.webclient_service.dto.TaskRequestDTO;
import com.project.webclient_service.dto.TaskResponseDTO;
import com.project.webclient_service.service.TaskService;
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
public class TaskController {

    private final TaskService taskService;

    @GetMapping("get-task")
    public ResponseEntity<Mono<TaskResponseDTO>> getTaskById(@RequestParam Long id) {
        log.info("Received request to get task by ID: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }


    @GetMapping("get-tasks")
    public ResponseEntity<Mono<List<TaskResponseDTO>>> getAllTasks() {
        log.info("Received request to get all tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }


    @PostMapping("create-task")
    public ResponseEntity<Mono<TaskResponseDTO>> createTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        log.info("Received request to create a task with title: {}", taskRequestDTO.getTaskTitle());
        return ResponseEntity.ok(taskService.createTask(taskRequestDTO));
    }


    @PutMapping("update-task")
    public ResponseEntity<Mono<TaskResponseDTO>> updateTask(@RequestBody TaskRequestDTO taskRequestDTO) {
        log.info("Received request to update a task with ID: {} and title: {}", taskRequestDTO.getTaskId(), taskRequestDTO.getTaskTitle());
        return ResponseEntity.ok(taskService.updateTask(taskRequestDTO));
    }


    @DeleteMapping("delete-task/{id}")
    public ResponseEntity<Mono<Void>> deleteTask(@PathVariable Long id) {
        log.info("Received request to delete task with ID: {}", id);
        return ResponseEntity.ok(taskService.deleteTask(id));
    }
}