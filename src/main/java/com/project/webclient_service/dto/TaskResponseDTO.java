package com.project.webclient_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskResponseDTO {
    private Long taskId;
    private String taskTitle;
    private String taskDescription;
    private LocalDateTime taskDueDate;
    private Long userId;
}