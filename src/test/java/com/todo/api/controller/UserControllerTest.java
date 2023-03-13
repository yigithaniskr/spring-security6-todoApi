package com.todo.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.todo.api.exception.NotUniqueEmailException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Role;
import com.todo.api.model.User;
import com.todo.api.request.RegisterRequest;
import com.todo.api.request.UserUpdateRequest;
import com.todo.api.response.UserResponse;
import com.todo.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

class UserControllerTest {
	
	private UserService userService;
	private UserController userController;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		userService = Mockito.mock(UserService.class);
		userController= new UserController(userService);
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
	}
	
	@Test
	public void testGetAllUsers() {
		List<UserResponse> userList =  new ArrayList<>();
		UserResponse user1 = UserResponse.builder()
				.id(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		UserResponse user2 = UserResponse.builder()
				.id(2L)
				.firstName("mehmet")
				.lastName("mehmet")
				.email("mehmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		userList.add(user1);
		userList.add(user2);
		
		Mockito.when(userService.getAllUsers()).thenReturn(userList);
		
		ResponseEntity<List<UserResponse>> response = userController.getAllUsers();
		
		assertEquals(ResponseEntity.ok().build().getStatusCode(), response.getStatusCode());
		assertIterableEquals(userList, response.getBody());	
	}
	
	@Test
	public void testFindUserByEmail() {
		String email = "ahmet@gmail.com";
		
		UserResponse user = UserResponse.builder()
				.id(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email(email)
				.password("password")
				.role(Role.USER)
				.build(); 
		
		Mockito.when(userService.findUserByEmail(email)).thenReturn(user);
		
		ResponseEntity<UserResponse> response = userController.findUserByEmail(email);
		
		assertEquals(ResponseEntity.ok().build().getStatusCode(), response.getStatusCode());
		assertEquals(user, response.getBody());
	}
	
	@Test
	public void findUserByEmail_UserNotFound() {
	    String nonExistingEmail = "nonexistingemail@example.com";

	    Mockito.when(userService.findUserByEmail(nonExistingEmail)).thenThrow(new UserNotFoundException("User Not Found!"));

	    UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
	        userController.findUserByEmail(nonExistingEmail);
	    });
	    assertEquals("User Not Found!", exception.getMessage());
	}
	
	@Test
	public void testCreateUser() throws Exception {
	    RegisterRequest registerRequest = RegisterRequest.builder()
	            .firstName("ahmet")
	            .lastName("ahmet")
	            .email("ahmet@gmail.com")
	            .password("password")
	            .role(Role.USER)
	            .build();
	    User user = User.builder()
	    		.id(1L)
	    		.firstName(registerRequest.getFirstName())
	    		.lastName(registerRequest.getLastName())
	    		.email(registerRequest.getEmail())
	    		.password(new BCryptPasswordEncoder().encode(registerRequest.getPassword()))
	    		.role(registerRequest.getRole())
	    		.build();
	    
	    UserResponse userResponse = new UserResponse(user);
	    
	    Mockito.when(userService.createUser(registerRequest)).thenReturn(userResponse);

	    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(new ObjectMapper().writeValueAsString(registerRequest))
	            .accept(MediaType.APPLICATION_JSON))
			    .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("ahmet"))
		        .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("ahmet"))
		        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("ahmet@gmail.com"))
		        .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("USER"));
	}
	
	@Test
	public void testCreateUser_whenUserExist_shouldReturnNotUniqueEmailException() throws Exception{
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		Mockito.when(userService.createUser(request)).thenThrow(new NotUniqueEmailException("There is a user in the system with this email address"));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testDeleteUser() throws Exception {
	    Long userId = 1L;
	    doNothing().when(userService).deleteUser(userId);
	    mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/user/{id}", userId))
	           .andExpect(MockMvcResultMatchers.status().isOk())
	           .andExpect(MockMvcResultMatchers.content().string(""));
	    Mockito.verify(userService, times(1)).deleteUser(userId);
	}
	
	@Test
	public void testDeleteUser_whenUserDoesNotExist_shouldReturnUserNotFoundException() throws Exception {
		Long nonExistingUserId  = 35L;
		doThrow(new UserNotFoundException("User Not Found!")).when(userService).deleteUser(nonExistingUserId);
        
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/user/{id}", nonExistingUserId))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void updateUserTest() throws Exception {
	    UserUpdateRequest request = new UserUpdateRequest();
	    request.setUserId(1L);
	    request.setFirstName("ahmet");
	    request.setLastName("ahmet");
	    request.setEmail("ahmet@gmail.com");
	    request.setPassword("password");

	    UserResponse userResponse = new UserResponse();
	    userResponse.setId(request.getUserId());
	    userResponse.setFirstName(request.getFirstName());
	    userResponse.setLastName(request.getLastName());
	    userResponse.setEmail(request.getEmail());
	    userResponse.setPassword(new BCryptPasswordEncoder().encode("password"));

	    Mockito.when(userService.updateUser(request)).thenReturn(userResponse);

	    mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
	            .content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
	            .andExpect(MockMvcResultMatchers.status().isOk())
	            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(request.getUserId()))
	            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(request.getFirstName()))
	            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(request.getLastName()));
	}
	
	@Test
	public void testUpdateUser_whenUserDoesNotExist_shouldReturnUserNotFoundException() throws Exception{
		UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		
		Mockito.when(userService.updateUser(userUpdateRequest)).thenThrow(new UserNotFoundException("User Not Found!"));
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(userUpdateRequest))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	public void testUpdateUser_whenExistingEmail_shouldReturnNotUniqeEmailException() throws Exception {
		UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		Mockito.when(userService.updateUser(userUpdateRequest)).thenThrow(new NotUniqueEmailException("There is a user in the system with this email address"));
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(userUpdateRequest))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	//  test invalid request
	
	@Test
	public void testCreateUser_withBlankFirstName_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testCreateUser_withBlankLastName_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testCreateUser_withBlankEmail_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testCreateUser_withWrongFormatEmail_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmetahmethotmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testCreateUser_withBlankPassword_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testCreateUser_withNullRole_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(null)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testUpdateUser_withNullUserId_shouldReturnBadRequest() throws Exception {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(null)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	@Test
	public void testUpdateUser_withBlankFirstName_shouldReturnBadRequest() throws Exception {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testUpdateUser_withBlankLastName_shouldReturnBadRequest() throws Exception {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("ahmet")
				.lastName("")
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	@Test
	public void testUpdateUser_withBlankEmail_shouldReturnBadRequest() throws Exception {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	@Test
	public void testUpdateUser_withWrongFormatEmail_shouldReturnBadRequest() throws Exception {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmethotmail.com")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testUpdateUser_withBlankPassword_shouldReturnBadRequest() throws Exception {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
}
