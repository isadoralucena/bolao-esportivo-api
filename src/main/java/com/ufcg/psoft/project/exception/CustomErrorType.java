package com.ufcg.psoft.project.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomErrorType {

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errors")
    private List<String> errors;

    public CustomErrorType(ProjectException exception, LocalDateTime timestamp) {
        this.timestamp = Objects.requireNonNull(timestamp, "O timestamp não pode ser nulo");
        this.message = exception.getMessage();
        this.errors = new ArrayList<>();
    }
}
