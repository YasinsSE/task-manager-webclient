package com.project.webclient_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserResponseDTO {
    private Long userId;
    private String fullName;
    private String userEmail;
    private String role;
    private Set<Long> taskIds;
}