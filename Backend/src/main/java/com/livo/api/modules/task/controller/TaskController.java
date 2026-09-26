package com.livo.api.modules.task.controller;

import com.livo.api.common.response.ApiResponse;
import com.livo.api.common.security.CurrentUser;
import com.livo.api.modules.task.dto.CreateSubtaskRequest;
import com.livo.api.modules.task.dto.CreateTaskRequest;
import com.livo.api.modules.task.dto.RescheduleTaskRequest;
import com.livo.api.modules.task.dto.SubtaskDto;
import com.livo.api.modules.task.dto.TaskResponse;
import com.livo.api.modules.task.dto.UpdateTaskRequest;
import com.livo.api.modules.task.entity.enums.TaskPriority;
import com.livo.api.modules.task.entity.enums.TaskStatus;
import com.livo.api.modules.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Core task management, subtasks, priorities, and AI subtask breakdown")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @CurrentUser UUID userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID goalId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TaskPriority priority
    ) {
        List<TaskResponse> tasks = taskService.getTasks(userId, status, dueDate, startDate, endDate, goalId, category, priority);
        return ResponseEntity.ok(ApiResponse.success(tasks, "Tasks retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        TaskResponse response = taskService.getTaskById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Task retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Task created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Task updated successfully"));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleTaskCompletion(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        TaskResponse response = taskService.toggleTaskCompletion(userId, id);
        String message = response.getStatus() == TaskStatus.COMPLETED ? "Task marked completed" : "Task marked incomplete";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TaskResponse>> startTask(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        TaskResponse response = taskService.startTask(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Task started successfully"));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<TaskResponse>> rescheduleTask(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @RequestBody RescheduleTaskRequest request
    ) {
        TaskResponse response = taskService.rescheduleTask(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Task rescheduled successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @CurrentUser UUID userId,
            @PathVariable UUID id
    ) {
        taskService.deleteTask(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));
    }

    @PostMapping("/{id}/subtasks")
    public ResponseEntity<ApiResponse<SubtaskDto>> addSubtask(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateSubtaskRequest request
    ) {
        SubtaskDto subtask = taskService.addSubtask(userId, id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subtask, "Subtask added successfully"));
    }

    @PatchMapping("/{taskId}/subtasks/{subtaskId}/toggle")
    public ResponseEntity<ApiResponse<SubtaskDto>> toggleSubtask(
            @CurrentUser UUID userId,
            @PathVariable UUID taskId,
            @PathVariable UUID subtaskId
    ) {
        SubtaskDto subtask = taskService.toggleSubtask(userId, taskId, subtaskId);
        return ResponseEntity.ok(ApiResponse.success(subtask, "Subtask toggled successfully"));
    }

    @DeleteMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubtask(
            @CurrentUser UUID userId,
            @PathVariable UUID taskId,
            @PathVariable UUID subtaskId
    ) {
        taskService.deleteSubtask(userId, taskId, subtaskId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subtask deleted successfully"));
    }
}
