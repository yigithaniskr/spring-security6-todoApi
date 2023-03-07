package com.todo.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todo.api.request.TodoRequest;
import com.todo.api.request.TodoUpdateRequest;
import com.todo.api.response.TodoResponse;
import com.todo.api.service.TodoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/todo")
@RequiredArgsConstructor
public class TodoController {
	
	private final TodoService todoService;
	
	@GetMapping("/{userId}")
	public ResponseEntity<List<TodoResponse>>  getAllTodos(@PathVariable Long userId){
		return ResponseEntity.ok(todoService.getAllTodos(userId));
	}
	
	@PostMapping 
	public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoRequest todoRequest) {
		return ResponseEntity.ok(todoService.createTodo(todoRequest));
	}

	@DeleteMapping("/delete/{todoId}")
	public ResponseEntity<Void> deleteTodo(@PathVariable Long todoId){
		todoService.deleteTodo(todoId);
		return ResponseEntity.ok().build();
	}
	
	@PutMapping()
	public ResponseEntity<TodoResponse> updateTodo(@Valid @RequestBody TodoUpdateRequest todoUpdateRequest){
		return ResponseEntity.ok(todoService.updateTodo(todoUpdateRequest));
	}
	
	@GetMapping("/active/{userId}/{active}")
	public ResponseEntity<List<TodoResponse>> getTodosByActive(@PathVariable boolean active, @PathVariable Long userId){
		return ResponseEntity.ok(todoService.findTodosByActive(active,userId));
	}
}
