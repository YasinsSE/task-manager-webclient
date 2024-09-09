package com.project.webclient_service.service.impl;

import com.project.webclient_service.dto.UserRequestDTO;
import com.project.webclient_service.dto.UserResponseDTO;
import com.project.webclient_service.service.UserService;
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
public class UserServiceImpl implements UserService {

    private final WebClient webClient;

    public static final String CIRCUIT_BREAKER_SERVICE = "taskManagerService";
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ConcurrentMap<Long, UserResponseDTO> userCache = new ConcurrentHashMap<>();

    @Override
    public Mono<List<UserResponseDTO>> getAllUsers() {
        log.info("getAllUsers method called to retrieve all users.");

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_SERVICE);

        return webClient.get()
                .uri("/user-api/list-users")
                .retrieve()
                .bodyToFlux(UserResponseDTO.class)
                .collectList()
                .doOnNext(this::cacheUsers) // Cache the users after successfully fetching them
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(CallNotPermittedException.class, e -> {
                    log.warn("Fallback triggered: Circuit breaker is open");
                    return fallbackUsers();
                })
                .doOnError(error -> log.error("Error occurred while fetching users", error));
    }

    // Cache users after successful fetch
    private void cacheUsers(List<UserResponseDTO> users) {
        log.info("Caching {} users after successful fetch", users.size());
        users.forEach(user -> userCache.put(user.getUserId(), user));
    }

    public Mono<List<UserResponseDTO>> fallbackUsers() {
        log.info("Returning fallback users due to circuit breaker being open.");

        if (userCache.isEmpty()) {
            log.warn("Cache is empty. No fallback data available.");
            return Mono.error(new RuntimeException("No cached users available and service is currently unavailable."));
        }

        List<UserResponseDTO> cachedUsers = userCache.values().stream().toList();
        return Mono.just(cachedUsers);
    }


    @Override
    public Mono<UserResponseDTO> getUserById(Long userId) {
        log.info("getUserById method called for user ID: {}", userId);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_SERVICE);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user-api/list-user")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .bodyToMono(UserResponseDTO.class)
                .doOnNext(this::cacheUser) // Cache single user after fetching
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(CallNotPermittedException.class, e -> {
                    log.warn("Fallback triggered: Circuit breaker is open for user ID: {}", userId);
                    return fallbackUserById(userId);
                })
                .doOnSuccess(user -> log.info("Successfully fetched user with ID: {} and email: {}", userId, user.getUserEmail()))
                .doOnError(error -> log.error("Error occurred while fetching user with ID: {}", userId, error));
    }

    // Cache single user after successful fetch
    private void cacheUser(UserResponseDTO user) {
        log.info("Caching user with ID: {} after successful fetch", user.getUserId());
        userCache.put(user.getUserId(), user);
    }

    public Mono<UserResponseDTO> fallbackUserById(Long userId) {
        log.info("Returning fallback user due to circuit breaker being open for user ID: {}", userId);

        UserResponseDTO cachedUser = userCache.get(userId);
        if (cachedUser == null) {
            log.warn("No cached user found for user ID: {}", userId);
            return Mono.error(new RuntimeException("No cached user available for user ID: " + userId + " and service is currently unavailable."));
        }

        return Mono.just(cachedUser);
    }


    @Override
    public Mono<UserResponseDTO> createUser(UserRequestDTO userRequestDTO) {
        log.info("createUser method called to create a user with email: '{}'", userRequestDTO.getUserEmail());
        return webClient.post()
                .uri("/user-api/create-user")
                .body(Mono.just(userRequestDTO), UserRequestDTO.class)
                .retrieve()
                .bodyToMono(UserResponseDTO.class)
                .doOnSuccess(user -> log.info("Successfully created user with ID: {} and email: {}", user.getUserId(), user.getUserEmail()))
                .doOnError(error -> log.error("Error occurred while creating user with email: {}", userRequestDTO.getUserEmail(), error));
    }


    @Override
    public Mono<UserResponseDTO> updateUser(UserRequestDTO userRequestDTO) {
        log.info("updateUser method called for user ID: {} with email: '{}'", userRequestDTO.getUserId(), userRequestDTO.getUserEmail());
        return webClient.put()
                .uri("/user-api/update-user")
                .body(Mono.just(userRequestDTO), UserRequestDTO.class)
                .retrieve()
                .bodyToMono(UserResponseDTO.class)
                .doOnSuccess(user -> log.info("Successfully updated user with ID: {} and email: {}", user.getUserId(), user.getUserEmail()))
                .doOnError(error -> log.error("Error occurred while updating user with ID: {} and email: {}", userRequestDTO.getUserId(), userRequestDTO.getUserEmail(), error));
    }


    @Override
    public Mono<Void> deleteUser(Long userId) {
        log.info("deleteUser method called for user ID: {}", userId);
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/user-api/delete-user/{id}")
                        .build(userId))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(aVoid -> log.info("Successfully deleted user with ID: {}", userId))
                .doOnError(error -> log.error("Error occurred while deleting user with ID: {}", userId, error));
    }
}