package com.todo.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.todo.api.config.JwtService;
import com.todo.api.exception.CustomBadCredentialsException;
import com.todo.api.exception.NotUniqueEmailException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Role;
import com.todo.api.model.User;
import com.todo.api.repository.UserRepository;
import com.todo.api.request.AuthenticationRequest;
import com.todo.api.request.RegisterRequest;
import com.todo.api.response.AuthenticationResponse;

class AuthenticationServiceTest {

	private UserRepository repository;
	private PasswordEncoder passwordEncoder;
	private JwtService jwtService;
	private AuthenticationManager authenticationManager;
	private AuthenticationService authenticationService;

	@BeforeEach
	public void setUp() {
		repository = Mockito.mock(UserRepository.class);
	    passwordEncoder = Mockito.mock(PasswordEncoder.class);
	    jwtService = Mockito.mock(JwtService.class);
	    authenticationManager = Mockito.mock(AuthenticationManager.class);
	    authenticationService = new AuthenticationService(repository, passwordEncoder, jwtService, authenticationManager);
	}
	
	@Test
	public void testRegister_shouldReturnAuthenticationResponse() {
		RegisterRequest request =  RegisterRequest.builder()
				.email("ahmet@gmail.com")
				.firstName("ahmet")
				.lastName("ahmet")
				.password("password")
				.role(Role.USER)
				.build();
		
		Mockito.when(repository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
		
		User user = User.builder()
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.password("encodedPassword")
				.role(request.getRole())
				.build();
		Mockito.when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
		Mockito.when(repository.save(any(User.class))).thenReturn(user);
		
		String token = "jwtToken";
		Mockito.when(jwtService.generateToken(user)).thenReturn(token);
		
		AuthenticationResponse response = authenticationService.register(request);
		
		assertEquals(token, response.getToken());
	}
	
	@Test
	public void testRegister_withNotUniqueEmail_shouldReturnNotUniqueEmailException() {
		RegisterRequest request =  RegisterRequest.builder()
				.email("ahmet@gmail.com")
				.firstName("ahmet")
				.lastName("ahmet")
				.password("password")
				.role(Role.USER)
				.build();
		
		User existingUser = User.builder()
	            .firstName("mehmet")
	            .lastName("mehmet")
	            .email(request.getEmail())
	            .password("encodedPassword")
	            .role(Role.USER)
	            .build();
		
		Mockito.when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));
		
		assertThrows(NotUniqueEmailException.class, () ->{
			authenticationService.register(request);
		});
	}
	
	@Test
	public void testAuthenticate_shouldReturnAuthenticationResponse() {
	    AuthenticationRequest request = AuthenticationRequest.builder()
	            .email("ahmet@gmail.com")
	            .password("password")
	            .build();

	    User user = User.builder()
	            .id(1L)
	            .firstName("ahmet")
	            .lastName("ahmet")
	            .email(request.getEmail())
	            .password("encodedPassword")
	            .role(Role.USER)
	            .build();

	    Mockito.when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

	    String token = "jwtToken";
	    Mockito.when(jwtService.generateToken(user)).thenReturn(token);

	    authenticationService.authenticate(request);
	    Authentication auth = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
	    Mockito.when(authenticationManager.authenticate(auth)).thenReturn(auth);
	    Mockito.verify(authenticationManager).authenticate(auth);
	}
	
	@Test
	public void testAuthenticate_whenUserNotFound_shouldReturnUserNotFoundException() {
		AuthenticationRequest request = AuthenticationRequest.builder()
	            .email("ahmet@gmail.com")
	            .password("password")
	            .build();
		
		Mockito.when(repository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
		assertThrows(UserNotFoundException.class, () -> {
	        authenticationService.authenticate(request);
	    });
	}
	
	@Test
	public void testAuthenticate_whenWrongMailOrPassword_shouldReturnBadCredentialsException() {
		AuthenticationRequest request = AuthenticationRequest.builder()
	            .email("invalid@gmail.com")
	            .password("invalidPassword")
	            .build();
		
	    Mockito.when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
	    Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenThrow(new CustomBadCredentialsException("Invalid email or password."));

	    assertThrows(CustomBadCredentialsException.class, () ->{
	    	authenticationService.authenticate(request);
	    });

		
		
	}
	

}
