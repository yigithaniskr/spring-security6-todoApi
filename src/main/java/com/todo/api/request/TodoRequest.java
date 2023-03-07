package com.todo.api.request;

import com.todo.api.request.TodoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoRequest {

	@NotBlank
	private String task;
	
	private boolean active =  Boolean.TRUE;
	
	@NotNull
    private Long userId;
}