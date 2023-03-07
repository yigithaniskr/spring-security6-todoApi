package com.todo.api.response;

import com.todo.api.model.Todo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoResponse {

	private Long id;
	private String task;
    private boolean active;
    private Long userId;
    
    public TodoResponse(Todo todo) {
    	this.id = todo.getId();
    	this.task = todo.getTask();
    	this.active = todo.isActive();
    	this.userId = todo.getUser().getId();
    }
}
