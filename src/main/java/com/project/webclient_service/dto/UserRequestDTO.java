package com.project.webclient_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserRequestDTO {
    private Long userId;
    private String fullName;
    private String userEmail;
    private String userPassword;
    private String role;
    private Set<Long> taskIds;
}