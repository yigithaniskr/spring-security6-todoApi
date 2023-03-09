package com.todo.api.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.todo.api.exception.CustomBadCredentialsException;
import com.todo.api.exception.NotUniqueEmailException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Role;
import com.todo.api.request.AuthenticationRequest;
import com.todo.api.request.RegisterRequest;
import com.todo.api.response.AuthenticationResponse;
import com.todo.api.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

class AuthenticationControllerTest {

	private MockMvc mockMvc;
	private AuthenticationService authenticationService;
	private AuthenticationController authenticationController;
	
	@BeforeEach
	void setUp() {
		authenticationService = Mockito.mock(AuthenticationService.class);
		authenticationController = new AuthenticationController(authenticationService);
		mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();

	}
	
	@Test
	public void testRegister() throws Exception{
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		String requestJson = new ObjectMapper().writeValueAsString(request);
		AuthenticationResponse response = AuthenticationResponse.builder()
				.token("token")
				.build();
		
		Mockito.when(authenticationService.register(request)).thenReturn(response);
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		
		String responseJson = result.getResponse().getContentAsString();
		AuthenticationResponse actualResponse = new ObjectMapper().readValue(responseJson, AuthenticationResponse.class);
		
		
		assertEquals(response, actualResponse);
	}
	
	@Test
	public void testRegister_whenExistEmail_shouldReturnNotUniqueEmailExcetion() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		String requestJson = new ObjectMapper().writeValueAsString(request);
		
		Mockito.when(authenticationService.register(request))
        .thenThrow(new NotUniqueEmailException("There is a user in the system with this email address"));
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
		        .contentType(MediaType.APPLICATION_JSON)
		        .content(requestJson))
		        .andExpect(MockMvcResultMatchers.status().isBadRequest())
		        .andReturn();
	}
	
	@Test
	public void testAuhtenticate() throws Exception{
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		
		AuthenticationResponse response = AuthenticationResponse.builder()
				.token("jwtToken")
				.userId(1L)
				.build();
		
		Mockito.when(authenticationService.authenticate(request)).thenReturn(response);
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value(response.getToken()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(response.getUserId()));
        
        Mockito.verify(authenticationService).authenticate(request);
	}
	
	@Test
	public void testAuthenticate_whenUserDoesNotExist_shouldReturnUserNotFoundException() throws Exception{
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("ahmet@gmail.com")
				.password("password")
				.build();
		
		Mockito.when(authenticationService.authenticate(request)).thenThrow(new UserNotFoundException("User not found"));
        String requestJson = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(authenticationService).authenticate(request);
	}
	
	@Test
	public void testAuthenticate_whenWrongMailOrPassword_shouldReturnBadRequest() throws Exception {
	    AuthenticationRequest request = AuthenticationRequest.builder()
	            .email("invalid@gmail.com")
	            .password("invalidPassword")
	            .build();
	    
	    Mockito.when(authenticationService.authenticate(request)).thenThrow(new CustomBadCredentialsException("Invalid email or password."));
	    
	    String requestJson = new ObjectMapper().writeValueAsString(request);
	    
	    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
	    		.contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
	    		.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	
	@Test
	public void testRegister_withBlankFirstName_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testRegister__withBlankLastName_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("")
				.email("ahmet@gmail.com")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testRegister__withBlankEmail_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testRegister__withWrongFormatEmail_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmetgmailcom")
				.password("password")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testRegister__withBlankPassword_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("")
				.role(Role.USER)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	@Test
	public void testRegister__withNullRole_shouldReturnBadRequest() throws Exception{
		
		RegisterRequest request = RegisterRequest.builder()
				.firstName("ahmet")
				.lastName("ahmet")
				.email("ahmet@gmail.com")
				.password("password")
				.role(null)
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testAuthenticate_withBlankEmail_shouldReturnBadRequest() throws Exception{
		
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	@Test
	public void testAuthenticate_withWrongFormatEmail_shouldReturnBadRequest() throws Exception{
		
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("ahmetmailcom")
				.password("password")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}
	
	@Test
	public void testAuthenticate_withBlankPassword_shouldReturnBadRequest() throws Exception{
		
		AuthenticationRequest request = AuthenticationRequest.builder()
				.email("ahmet@gmail.com")
				.password("")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(request))
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

}
