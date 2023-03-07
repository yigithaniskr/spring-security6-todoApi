package com.todo.api.request;

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
public class TodoUpdateRequest {

	@NotNull
	private Long todoId;
	
	@NotBlank
	private String task;
	
	@NotNull
    private boolean active;
}
