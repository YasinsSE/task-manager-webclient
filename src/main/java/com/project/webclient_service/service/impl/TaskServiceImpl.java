package com.project.webclient_service.service.impl;

import com.project.webclient_service.dto.TaskRequestDTO;
import com.project.webclient_service.dto.TaskResponseDTO;
import com.project.webclient_service.service.TaskService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final WebClient webClient;

    public static final String CIRCUIT_BREAKER_SERVICE = "taskManagerService";
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    // In-memory cache to store tasks temporarily
    private final ConcurrentMap<Long, TaskResponseDTO> taskCache = new ConcurrentHashMap<>();

    @Override
    public Mono<List<TaskResponseDTO>> getAllTasks() {
        log.info("getAllTasks method called to retrieve all tasks.");

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_SERVICE);

        return webClient.get()
                .uri("/task-api/list-tasks")
                .retrieve()
                .bodyToFlux(TaskResponseDTO.class)
                .collectList()
                .doOnNext(this::cacheTasks) // Cache the tasks after successfully fetching them
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(CallNotPermittedException.class, e -> {
                    log.warn("Fallback triggered: Circuit breaker is open");
                    return fallbackTasks();
                })
                .doOnError(error -> log.error("Error occurred while fetching tasks", error));
    }

    // Cache tasks after successful fetch
    private void cacheTasks(List<TaskResponseDTO> tasks) {
        log.info("Caching {} tasks after successful fetch", tasks.size());
        tasks.forEach(task -> taskCache.put(task.getTaskId(), task));
    }

    public Mono<List<TaskResponseDTO>> fallbackTasks() {
        log.info("Returning fallback tasks due to circuit breaker being open.");

        if (taskCache.isEmpty()) {
            log.warn("Cache is empty. No fallback data available.");
            return Mono.error(new RuntimeException("No cached tasks available and service is currently unavailable."));
        }

        List<TaskResponseDTO> cachedTasks = taskCache.values().stream().toList();
        return Mono.just(cachedTasks);
    }


    @Override
    public Mono<TaskResponseDTO> getTaskById(Long taskId) {
        log.info("getTaskById method called for task ID: {}", taskId);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_SERVICE);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/task-api/list-task")
                        .queryParam("taskId", taskId)
                        .build())
                .retrieve()
                .bodyToMono(TaskResponseDTO.class)
                .doOnNext(this::cacheTask) // Cache single task after fetching
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(CallNotPermittedException.class, e -> {
                    log.warn("Fallback triggered: Circuit breaker is open for task ID: {}", taskId);
                    return fallbackTaskById(taskId);
                })
                .doOnSuccess(task -> log.info("Successfully fetched task with ID: {} and title: {}", task.getTaskId(), task.getTaskTitle()))
                .doOnError(error -> log.error("Error occurred while fetching task with ID: {}", taskId, error));
    }

    // Cache single task after successful fetch
    private void cacheTask(TaskResponseDTO task) {
        log.info("Caching task with ID: {} after successful fetch", task.getTaskId());
        taskCache.put(task.getTaskId(), task);
    }

    public Mono<TaskResponseDTO> fallbackTaskById(Long taskId) {
        log.info("Returning fallback task due to circuit breaker being open for task ID: {}", taskId);

        TaskResponseDTO cachedTask = taskCache.get(taskId);
        if (cachedTask == null) {
            log.warn("No cached task found for task ID: {}", taskId);
            return Mono.error(new RuntimeException("No cached task available for task ID: " + taskId + " and service is currently unavailable."));
        }

        return Mono.just(cachedTask);
    }


    @Override
    public Mono<TaskResponseDTO> createTask(TaskRequestDTO taskRequestDTO) {
        log.info("createTask method called to create a task with title: '{}'", taskRequestDTO.getTaskTitle());
        return webClient.post()
                .uri("/task-api/create-task")
                .body(Mono.just(taskRequestDTO), TaskRequestDTO.class)
                .retrieve()
                .bodyToMono(TaskResponseDTO.class)
                .doOnSuccess(task -> log.info("Successfully created task with ID: {} and title: {}", task.getTaskId(), task.getTaskTitle()))
                .doOnError(error -> log.error("Error occurred while creating task with title: {}", taskRequestDTO.getTaskTitle(), error));
    }


    @Override
    public Mono<TaskResponseDTO> updateTask(TaskRequestDTO taskRequestDTO) {
        log.info("updateTask method called for task ID: {} with title: '{}'", taskRequestDTO.getTaskId(), taskRequestDTO.getTaskTitle());
        return webClient.put()
                .uri("/task-api/update-task")
                .body(Mono.just(taskRequestDTO), TaskRequestDTO.class)
                .retrieve()
                .bodyToMono(TaskResponseDTO.class)
                .doOnSuccess(task -> log.info("Successfully updated task with ID: {} and title: {}", task.getTaskId(), task.getTaskTitle()))
                .doOnError(error -> log.error("Error occurred while updating task with ID: {} and title: {}", taskRequestDTO.getTaskId(), taskRequestDTO.getTaskTitle(), error));
    }


    @Override
    public Mono<Void> deleteTask(Long taskId) {
        log.info("deleteTask method called for task ID: {}", taskId);
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/task-api/delete-task")
                        .queryParam("taskId", taskId)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(aVoid -> log.info("Successfully deleted task with ID: {}", taskId))
                .doOnError(error -> log.error("Error occurred while deleting task with ID: {}", taskId, error));
    }
}

