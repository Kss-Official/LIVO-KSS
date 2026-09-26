package com.livo.api.modules.task.service;

import com.livo.api.modules.task.dto.CreateSubtaskRequest;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.RescheduleTaskRequest;
import com.livo.api.modules.task.dto.SubtaskDto;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.dto.UpdateTaskRequest;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskService {

    List<TaskResponse> getTasks(
            UUID userId,
            TaskStatus status,
            LocalDate dueDate,
            LocalDate startDate,
            LocalDate endDate,
            UUID goalId,
            String category,
            TaskPriority priority
    );

    TaskResponse getTaskById(UUID userId, UUID taskId);

    TaskResponse createTask(UUID userId, CreateTaskRequest request);

    TaskResponse updateTask(UUID userId, UUID taskId, UpdateTaskRequest request);

    TaskResponse toggleTaskCompletion(UUID userId, UUID taskId);

    TaskResponse startTask(UUID userId, UUID taskId);

    TaskResponse rescheduleTask(UUID userId, UUID taskId, RescheduleTaskRequest request);

    void deleteTask(UUID userId, UUID taskId);

    SubtaskDto addSubtask(UUID userId, UUID taskId, CreateSubtaskRequest request);

    SubtaskDto toggleSubtask(UUID userId, UUID taskId, UUID subtaskId);

    void deleteSubtask(UUID userId, UUID taskId, UUID subtaskId);
}
