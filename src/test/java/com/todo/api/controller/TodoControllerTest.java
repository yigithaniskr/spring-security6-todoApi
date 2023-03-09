package com.todo.api.controller;

import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.todo.api.exception.TodoNotFoundException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Role;
import com.todo.api.model.Todo;
import com.todo.api.model.User;
import com.todo.api.request.TodoRequest;
import com.todo.api.request.TodoUpdateRequest;
import com.todo.api.response.TodoResponse;
import com.todo.api.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;

class TodoControllerTest {
	
	private TodoService todoService;
	private TodoController todoController;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		todoService = Mockito.mock(TodoService.class);
		todoController = new TodoController(todoService);
		mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();

	}

	@Test
	public void testGetAllTodos() throws Exception{
		User user = User.builder()
				.id(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		Todo todo1 = Todo.builder()
				.id(1L)
				.task("do something")
				.active(true)
				.user(user)
				.build();
		Todo todo2 = Todo.builder()
				.id(2L)
				.task("do something")
				.active(true)
				.user(user)
				.build();
		List<Todo> todos  = List.of(todo1,todo2);
		List<TodoResponse> expectedResponse = todos.stream().map(todo ->new TodoResponse(todo)).collect(Collectors.toList());
		
		Mockito.when(todoService.getAllTodos(user.getId())).thenReturn(expectedResponse);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/todo/{userId}", user.getId()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.iterableWithSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(todo1.getId().intValue()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(todo2.getId().intValue()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].task").value(todo1.getTask()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].task").value(todo2.getTask()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].active").value(todo1.isActive()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].active").value(todo2.isActive()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].userId").value(user.getId().intValue()))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].userId").value(user.getId().intValue()));
	}
	
	@Test
	public void testGetAllTodos_whenUserDoesNotExist_shouldReturnUserNotFoundException() throws Exception{
		Long userId = 1L;
		
		Mockito.when(todoService.getAllTodos(userId)).thenThrow(new UserNotFoundException("User Not Found!"));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/todo/{userId}",userId))
			.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testGetTodosByActive() throws Exception{
		User user = User.builder()
				.id(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		Todo todo1 = Todo.builder()
				.id(1L)
				.task("do something")
				.active(true)
				.user(user)
				.build();
		Todo todo2 = Todo.builder()
				.id(2L)
				.task("do something")
				.active(true)
				.user(user)
				.build();
		List<Todo> todos  = List.of(todo1,todo2);
		List<TodoResponse> expectedResponse = todos.stream().map(todo ->new TodoResponse(todo)).collect(Collectors.toList());
		
		Mockito.when(todoService.findTodosByActive(true, user.getId())).thenReturn(expectedResponse);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/todo/active/{userId}/{active}",user.getId(),true))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.iterableWithSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(todo1.getId().intValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(todo2.getId().intValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].task").value(todo1.getTask()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].task").value(todo2.getTask()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].active").value(todo1.isActive()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].active").value(todo2.isActive()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].userId").value(todo1.getUser().getId().intValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].userId").value(todo2.getUser().getId().intValue()));
	}
	@Test
	public void testGetTodosByActive_whenUserDoesNotExist_shouldReturnUserNotFoundException() throws Exception{
		Long userId=1L;
		boolean active = true;
		
		Mockito.when(todoService.findTodosByActive(active, userId)).thenThrow(new UserNotFoundException("User Not Found!"));
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/todo/active/{userId}/{active}",userId, active))
			.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testDeleteTodo() throws Exception {
	    Long todoId = 1L;

	    mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/todo/delete/{todoId}", todoId))
	            .andExpect(MockMvcResultMatchers.status().isOk());

	    Mockito.verify(todoService, Mockito.times(1)).deleteTodo(todoId);
	}
	
	@Test
	public void testDeleteTodo_whenTodoNotFound_shouldReturnTodoNotFoundException() throws Exception{
	    Long todoId = 1L;
	    
	    doThrow(new TodoNotFoundException("Todo Not Found!")).when(todoService).deleteTodo(todoId);
	    
	    mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/todo/delete/{todoId}", todoId))
	        .andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testCreateTodo() throws Exception {
		
		TodoRequest request = TodoRequest.builder()
				.task("do something")
				.active(true)
				.userId(2L)
				.build();
		
		TodoResponse response = TodoResponse.builder()
				.id(1L)
				.task(request.getTask())
				.active(request.isActive())
				.userId(2L)
				.build();	
		
		Mockito.when(todoService.createTodo(request)).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/todo")
			    .contentType(MediaType.APPLICATION_JSON)
			    .content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(response.getId().intValue()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.task").value(response.getTask()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.active").value(response.isActive()));
			
	}
	
	@Test
	public void testCreateTodo_whenUserDoesNotExist_shouldReturnUserNotFoundException() throws Exception{
		TodoRequest request = TodoRequest.builder()
				.task("do something")
				.active(true)
				.userId(2L)
				.build();
		
		Mockito.when(todoService.createTodo(request)).thenThrow(new UserNotFoundException("User Not Found!"));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/todo")
			    .contentType(MediaType.APPLICATION_JSON)
			    .content(new ObjectMapper().writeValueAsString(request)))
		.andExpect(MockMvcResultMatchers.status().isNotFound());

	}
	
	@Test
	public void testUpdateTodo() throws Exception{
		TodoUpdateRequest request = TodoUpdateRequest.builder()
				.todoId(1L)
				.task("do something")
				.active(false)
				.build();
		
		TodoResponse response = TodoResponse.builder()
				.task(request.getTask())
				.active(request.isActive())
				.build();
		
		Mockito.when(todoService.updateTodo(request)).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/todo")
				.contentType(MediaType.APPLICATION_JSON)
			    .content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.task").value(request.getTask()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.active").value(request.isActive()));
	}
	
	@Test
	public void testUpdateTodo_whenTodoDoesNotExist_shouldReturnTodoNotFoundException() throws Exception{
		TodoUpdateRequest request = TodoUpdateRequest.builder()
				.todoId(1L)
				.task("do something")
				.active(false)
				.build();
		
		Mockito.when(todoService.updateTodo(request)).thenThrow(new TodoNotFoundException("Todo Not Found!"));
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/todo")
				.contentType(MediaType.APPLICATION_JSON)
			    .content(new ObjectMapper().writeValueAsString(request)))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	// test invalid request
	
	@Test 
	public void testCreateTodo_withBlankTask_shouldReturnBadRequest() throws Exception{
		TodoRequest request = TodoRequest.builder()
				.task("")
				.active(true)
				.userId(2L)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/todo")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test 
	public void testCreateTodo_withNullUserId_shouldReturnBadRequest() throws Exception{
		TodoRequest request = TodoRequest.builder()
				.task("do something")
				.active(true)
				.userId(null)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/todo")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testupdateTodo_withNullTodoId_shouldReturnBadRequest() throws Exception{
		TodoUpdateRequest request = TodoUpdateRequest.builder()
				.todoId(null)
				.task("do something")
				.active(true)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/todo")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());	
	}
	
	@Test
	public void testupdateTodo_withBlankTask_shouldReturnBadRequest() throws Exception{
		TodoUpdateRequest request = TodoUpdateRequest.builder()
				.todoId(1L)
				.task("")
				.active(true)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/todo")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());	
	}
}
