package com.todo.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.todo.api.exception.TodoNotFoundException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Role;
import com.todo.api.model.Todo;
import com.todo.api.model.User;
import com.todo.api.repository.TodoRepository;
import com.todo.api.repository.UserRepository;
import com.todo.api.request.TodoRequest;
import com.todo.api.request.TodoUpdateRequest;
import com.todo.api.response.TodoResponse;

class TodoServiceTest {
	
	private TodoRepository todoRepository;
	private UserRepository userRepository;
	private TodoService todoService;

	@BeforeEach
	void setUp() {
		todoRepository = Mockito.mock(TodoRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		todoService = new TodoService(todoRepository, userRepository);
	}
	
	@Test
	public void testGetAllTodos_shouldReturnTodoResponseList() {
		User user = User.builder()
				.id(1L)
				.firstName("Ali")
				.lastName("Ali")
				.email("ali@gmail.com")
				.password("ali")
				.role(Role.USER)
				.build();
		Todo todo1 = Todo.builder()
				.id(1L)
				.task("Yemek yap")
				.active(true)
				.user(user)
				.build();
		Todo todo2 = Todo.builder()
				.id(2L)
				.task("Spor yap")
				.active(true)
				.user(user)
				.build();
		List<Todo> todos = List.of(todo1,todo2);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		Mockito.when(todoRepository.findByUserId(1L)).thenReturn(todos);
		
		List<TodoResponse> expected = todos.stream().map(todo -> new TodoResponse(todo)).collect(Collectors.toList());
		List<TodoResponse> result  = todoService.getAllTodos(1L);
		
		assertIterableEquals(expected, result);
		
		Mockito.verify(userRepository).findById(1L);
		Mockito.verify(todoRepository).findByUserId(1L);
	}
	
	@Test
	public void testGetAllTodos_whenUserDoesNotExist_shouldReturnUserNotFoundException() {
		Long userId =1L;
		Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, () ->{
			todoService.getAllTodos(1L);
		});
	}
	
	@Test
	public void testDeleteTodo_shoulDeleteTodo() {
		User user = User.builder()
				.id(1L)
				.firstName("Ali")
				.lastName("Ali")
				.email("ali@gmail.com")
				.password("ali")
				.role(Role.USER)
				.build();
		Todo todo1 = Todo.builder()
				.id(1L)
				.task("Yemek yap")
				.active(true)
				.user(user)
				.build();
		
		Mockito.when(todoRepository.findById(1L)).thenReturn(Optional.of(todo1));
		todoService.deleteTodo(1L);
		
		Mockito.verify(todoRepository).deleteById(1L);
	}
	
	@Test
	public void testDeleteTodo_whenTodoDoesNotExist_shoulReturnTodoNotFoundException() {
		Long id = 1L;
		Mockito.when(todoRepository.findById(id)).thenReturn(Optional.empty());
		
		assertThrows(TodoNotFoundException.class, () -> {
		    todoService.deleteTodo(1L);
		});
	}
	
	@Test
	public void testFindTodosByActive_shouldReturnTodoResponseList() {
		User user = User.builder()
				.id(1L)
				.firstName("Ali")
				.lastName("Ali")
				.email("ali@gmail.com")
				.password("ali")
				.role(Role.USER)
				.build();
		Todo todo1 = Todo.builder()
				.id(1L)
				.task("Yemek yap")
				.active(true)
				.user(user)
				.build();
		Todo todo2 = Todo.builder()
				.id(2L)
				.task("Spor yap")
				.active(true)
				.user(user)
				.build();
		List<Todo> todos = List.of(todo1,todo2);
		
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		Mockito.when(todoRepository.findByActiveAndUserId(true, 1L)).thenReturn(todos);
		
		List<TodoResponse> expected = todos.stream().filter(Todo::isActive).map(todo ->new TodoResponse(todo)).collect(Collectors.toList());
		
		List<TodoResponse> result = todoService.findTodosByActive(true, 1L);
		
		assertIterableEquals(expected, result);
		
		Mockito.verify(userRepository).findById(1L);
		Mockito.verify(todoRepository).findByActiveAndUserId(true, 1L);
	}
	
	@Test
	public void testFindTodosByActive_whenUserDoesNotExist_shouldReturnUserNotFoundException() {
		Long userId = 1L;
		Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, () -> {
			todoService.findTodosByActive(true, userId);
		});
		
		Mockito.verify(userRepository).findById(userId);
		Mockito.verifyNoInteractions(todoRepository);
	}
	
	@Test
	public void testCreateTodo_shouldReturnTodoResponse() {
		User user = User.builder()
				.id(1L)
				.firstName("Ali")
				.lastName("Ali")
				.email("ali@gmail.com")
				.password("ali")
				.role(Role.USER)
				.build();
		
		TodoRequest todoRequest  = TodoRequest.builder()
				.task("Yemek yap")
				.active(true)
				.userId(user.getId())
				.build();
		
		Todo todo = Todo.builder()
				.active(todoRequest.isActive())
				.task(todoRequest.getTask())
				.user(user)
				.build();
		
		Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		Mockito.when(todoRepository.save(Mockito.any(Todo.class))).thenReturn(todo);
		
		TodoResponse expected = new TodoResponse(todo);
		TodoResponse result = todoService.createTodo(todoRequest);
		
		assertEquals(expected, result);
		
		Mockito.verify(userRepository).findById(1L);
		Mockito.verify(todoRepository).save(Mockito.any(Todo.class));
	}
	
	@Test
	public void testCreateTodo_whenUserDoesNotExist_shouldReturnUserNotFoundException() {
		Long userId = 1L;
		TodoRequest todoRequest  = TodoRequest.builder()
				.task("Yemek yap")
				.active(true)
				.userId(userId)
				.build();
		
		Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, ()->{
			todoService.createTodo(todoRequest);
		});
		
		Mockito.verify(userRepository).findById(userId);
	}
	
	@Test
	public void testUpdateTodo_shouldReturnTodoResponse() {
		User user = User.builder()
				.id(1L)
				.firstName("Ali")
				.lastName("Ali")
				.email("ali@gmail.com")
				.password("ali")
				.role(Role.USER)
				.build();
		
		Todo existingTodo = Todo.builder()
				.id(1L)
				.task("Yemek yap")
				.active(true)
				.user(user)
				.build();
		
		TodoUpdateRequest request = TodoUpdateRequest.builder()
				.todoId(existingTodo.getId())
				.task("Spor Yap")
				.active(false)
				.build();
		
		existingTodo.setTask(request.getTask());
		existingTodo.setActive(request.isActive());
		
		Mockito.when(todoRepository.findById(existingTodo.getId())).thenReturn(Optional.of(existingTodo));
		Mockito.when(todoRepository.save(Mockito.any(Todo.class))).thenReturn(existingTodo);
		
		TodoResponse expected = new TodoResponse(existingTodo);
		TodoResponse result  = todoService.updateTodo(request);
		
		assertEquals(expected, result);
		
		Mockito.verify(todoRepository).findById(existingTodo.getId());
	    Mockito.verify(todoRepository).save(Mockito.any(Todo.class));
	}
	
	@Test
	public void testUpdateTodo_whenTodoDoesNotExist_shouldReturnTodoNotFoundException() {
		Long todoId =1L;
		
		TodoUpdateRequest request = TodoUpdateRequest.builder()
	            .todoId(todoId)
	            .task("Spor Yap")
	            .active(false)
	            .build();
	    Mockito.when(todoRepository.findById(todoId)).thenReturn(Optional.empty());
	    
	    assertThrows(TodoNotFoundException.class, () -> {
	        todoService.updateTodo(request);
	    });

	    Mockito.verify(todoRepository).findById(todoId);

	}	
}
