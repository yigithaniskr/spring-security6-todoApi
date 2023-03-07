package com.todo.api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.todo.api.exception.TodoNotFoundException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Todo;
import com.todo.api.model.User;
import com.todo.api.repository.TodoRepository;
import com.todo.api.repository.UserRepository;
import com.todo.api.request.TodoRequest;
import com.todo.api.request.TodoUpdateRequest;
import com.todo.api.response.TodoResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
	
	private final TodoRepository todoRepository;
	private final UserRepository userRepository;
	
	public List<TodoResponse> getAllTodos(Long id) {
		Optional<User> user = userRepository.findById(id);
		if(user.isPresent()) {
			List<Todo> todos = todoRepository.findByUserId(id);
			List<TodoResponse> todosResponse = todos.stream().map(todo -> new TodoResponse(todo)).collect(Collectors.toList());
			return todosResponse;
		}
		else {
			throw new UserNotFoundException("User Not Found!");
		}
	}
	
	@Transactional
	public TodoResponse createTodo(TodoRequest request) {
		Optional<User> user = userRepository.findById(request.getUserId());
		if(user.isPresent()) {
			Todo todo =  new Todo();
			todo.setTask(request.getTask());
			todo.setActive(request.isActive());
			todo.setUser(user.get());
			todoRepository.save(todo);
			TodoResponse response  = new TodoResponse(todo);
			return response;
		}
		else {
			throw new UserNotFoundException("User Not Found!");
		}
	}
	
	@Transactional
	public void deleteTodo(Long todoId) {
		Optional<Todo> todo = todoRepository.findById(todoId);
		if(todo.isPresent()) {
			todoRepository.deleteById(todoId);
		}
		else {
			throw new TodoNotFoundException("Todo Not Found!");
		}
	}
	
	@Transactional
	public TodoResponse updateTodo(TodoUpdateRequest todoUpdateRequest) {
		Optional<Todo> todo = todoRepository.findById(todoUpdateRequest.getTodoId());
		if(todo.isPresent()) {
			Todo todoObj =  todo.get();
			todoObj.setTask(todoUpdateRequest.getTask());
			todoObj.setActive(todoUpdateRequest.isActive());
			todoRepository.save(todoObj);
			TodoResponse response = new TodoResponse(todoObj);
			return response;
		}
		else {
			throw new TodoNotFoundException("Todo Not Found!");
		}
	}
	
	public List<TodoResponse> findTodosByActive(boolean active, Long userId){
		Optional<User> user =  userRepository.findById(userId);
		if(user.isPresent()) {
			List<Todo> todos = todoRepository.findByActiveAndUserId(active,userId);
			List<TodoResponse> todosResponse  = todos.stream().map(todo -> new TodoResponse(todo)).collect(Collectors.toList());
			return todosResponse;
		}
		else {
			throw new UserNotFoundException("User Not Found!");
		}
	}
}

