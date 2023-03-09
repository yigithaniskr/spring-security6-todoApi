package com.todo.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.todo.api.exception.NotUniqueEmailException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.Role;
import com.todo.api.model.User;
import com.todo.api.repository.UserRepository;
import com.todo.api.request.RegisterRequest;
import com.todo.api.request.UserUpdateRequest;
import com.todo.api.response.UserResponse;

class UserServiceTest {
	
	private UserRepository userRepository;
	private UserService userService;

	@BeforeEach
	void setUp() {
		userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
	}
	
	@Test
	public void testGetAllUsers_shouldReturnUserResponseList() {
		User user1 = User.builder()
				.id(1L)
				.firstName("Ahmet")
				.lastName("Ahmet")
				.email("ahmet@gmail.com")
				.password("ahmet85")
				.build();
		
		User user2 = User.builder()
				.id(1L)
				.firstName("Mehmet")
				.lastName("Mehmet")
				.email("mehmet@gmail.com")
				.password("ahmet85")
				.build();
		
		List<User> userList = new ArrayList<>();
		userList.add(user1);
		userList.add(user2);
		
		Mockito.when(userRepository.findAll()).thenReturn(userList);
		List<UserResponse> result = userService.getAllUsers();
		List<UserResponse> expected = new ArrayList<>();
		expected.add(new UserResponse(user1));
		expected.add(new UserResponse(user2));
		
		assertIterableEquals(expected, result);
		
		Mockito.verify(userRepository).findAll();
	}
	
	@Test
	public void testFindUserByEmail_whenUserExist_shouldReturnUserResponse() {
		User user1 = User.builder()
				.id(1L)
				.firstName("Ahmet")
				.lastName("Ahmet")
				.email("ahmet@gmail.com")
				.password("ahmet85")
				.build();
		
		Mockito.when(userRepository.findByEmail("ahmet@gmail.com")).thenReturn(Optional.of(user1));
		
		UserResponse result = userService.findUserByEmail("ahmet@gmail.com");
		
		UserResponse expected = new UserResponse(user1);
		
		assertEquals(expected, result);
		
		Mockito.verify(userRepository).findByEmail("ahmet@gmail.com");
	}
	
	@Test
	public void testFindUserByEmail_whenUserDoesNotExist_shouldReturnUserNotFoundException() {
		Mockito.when(userRepository.findByEmail("ahmet@gmail.com")).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, () ->{
			userService.findUserByEmail("ahmet@gmail.com");
		});
		
		Mockito.verify(userRepository).findByEmail("ahmet@gmail.com");
	}
	
	
	@Test
	public void testDeleteUser_whenUserExists() {
		User user1 = User.builder()
				.id(1L)
				.firstName("Ahmet")
				.lastName("Ahmet")
				.email("ahmet@gmail.com")
				.password("ahmet85")
				.build();
		
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
		
		userService.deleteUser(1L);
		
		Mockito.verify(userRepository).deleteById(1L);
	}
	
	@Test
	public void testDeleteUser_whenUserDoesNotExists() {
		Mockito.when(userRepository.findById(2L)).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, () ->{
			userService.deleteUser(2L);
		});
		
		Mockito.verify(userRepository).findById(2L);
	}
	
	@Test
	public void testCreateUser_shouldReturnUserResponse() {
	    
	    RegisterRequest registerRequest = RegisterRequest.builder()
	            .firstName("Ahmet")
	            .lastName("Ahmet")
	            .email("ahmet@gmail.com")
	            .password("ahmet85")
	            .role(Role.USER)
	            .build();
	    User expectedUser = new User();
	    expectedUser.setFirstName(registerRequest.getFirstName());
	    expectedUser.setLastName(registerRequest.getLastName());
	    expectedUser.setEmail(registerRequest.getEmail());
	    String encodedPassword = new BCryptPasswordEncoder().encode(registerRequest.getPassword());
	    expectedUser.setPassword(encodedPassword);
	    expectedUser.setRole(registerRequest.getRole());

	    Mockito.when(userRepository.findByEmail(registerRequest.getEmail()))
	            .thenReturn(Optional.empty());
	    Mockito.when(userRepository.save(Mockito.any(User.class)))
	            .thenReturn(expectedUser);

	    UserResponse userResponse = userService.createUser(registerRequest);

	    
	    assertEquals(expectedUser.getId(), userResponse.getId());
	    assertEquals(expectedUser.getFirstName(), userResponse.getFirstName());
	    assertEquals(expectedUser.getLastName(), userResponse.getLastName());
	    assertEquals(expectedUser.getEmail(), userResponse.getEmail());
	    assertEquals(expectedUser.getRole(), userResponse.getRole());
	    
	    
	}
	
	@Test
	public void testCreateUser_whenEmailExist_shouldReturnNotUniqueEmailException() {
		
		RegisterRequest registerRequest = RegisterRequest.builder()
	            .firstName("Ahmet")
	            .lastName("Ahmet")
	            .email("ahmet@gmail.com")
	            .password("ahmet85")
	            .role(Role.USER)
	            .build();
		User existingUser = User.builder()
				.firstName("Mehmet")
				.lastName("Mehmet")
				.email("ahmet@gmail.com")
				.password("mehmet89")
				.role(Role.USER)
				.build();
		
		Mockito.when(userRepository.findByEmail("ahmet@gmail.com")).thenReturn(Optional.of(existingUser));
		
		assertThrows(NotUniqueEmailException.class, () ->{
			userService.createUser(registerRequest);
		});
		
	}
	
	@Test
	public void testUpdateUser_shouldReturnUserResponse() {
		User existingUser = User.builder()
				.id(1L)
				.firstName("Mehmet")
				.lastName("Mehmet")
				.email("ahmet@gmail.com")
				.password("mehmet89")
				.role(Role.USER)
				.build();
		
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("Çetin")
				.lastName("Mehmet")
				.email("yalcin@gmail.com")
				.password("yalcin99")
				.build();
		
	    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
	    Mockito.when(userRepository.findByEmail("yalcin@gmail.com")).thenReturn(Optional.empty());
	    
	    UserResponse response = userService.updateUser(request);
	    
	    assertNotNull(response);
	    assertEquals(1L, response.getId());
	    assertEquals("Çetin", response.getFirstName());
	    assertEquals("Mehmet", response.getLastName());
	    assertEquals("yalcin@gmail.com", response.getEmail());
	    
	    Mockito.verify(userRepository).findById(1L);
	    Mockito.verify(userRepository).findByEmail("yalcin@gmail.com");
	}
	@Test
	public void testUpdateUser_ThrowsNotUniqueEmailException_WhenUpdatingWithExistingEmail() {
	    // Arrange
	    User existingUser = User.builder()
	    		.id(1L)
	    		.firstName("Ahmet")
	    		.lastName("Ahmet")
	    		.email("ahmet@gmail.com")
	    		.password("ahmet")
	    		.build();
	    UserUpdateRequest request = UserUpdateRequest.builder()
	    		.userId(1L)
	    		.firstName("Mehmet")
	    		.lastName("Mehmet")
	    		.email("mehmet@gmail.com")
	    		.password("mehmet")
	    		.build();
	    
	    User anotherUser = User.builder()
	    		.id(2L)
	    		.firstName("Ali")
	    		.lastName("Ali")
	    		.email("mehmet@gmail.com")
	    		.password("ali")
	    		.build();
	    
	    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
	    Mockito.when(userRepository.findByEmail("mehmet@gmail.com")).thenReturn(Optional.of(anotherUser));
	    
	    assertThrows(NotUniqueEmailException.class, () ->{
	    	userService.updateUser(request);
	    });
	    
	    Mockito.verify(userRepository).findById(1L);
	    Mockito.verify(userRepository).findByEmail("mehmet@gmail.com");
	    
	}
	@Test
	public void testUpdateUser_withSameEmail_shouldReturnUserResponse() {
		User existingUser = User.builder()
				.id(1L)
				.firstName("Mehmet")
				.lastName("Mehmet")
				.email("ahmet@gmail.com")
				.password("mehmet89")
				.role(Role.USER)
				.build();
		
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("Çetin")
				.lastName("Mehmet")
				.email("ahmet@gmail.com")
				.password("mehmet89")
				.build();
		
	    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
	    Mockito.when(userRepository.findByEmail("ahmet@gmail.com")).thenReturn(Optional.of(existingUser));
	    
	    UserResponse response = userService.updateUser(request);
	    
	    assertNotNull(response);
	    assertEquals(1L, response.getId());
	    assertEquals("Çetin", response.getFirstName());
	    assertEquals("Mehmet", response.getLastName());
	    assertEquals("ahmet@gmail.com", response.getEmail());
	    
	    Mockito.verify(userRepository).findById(1L);
	    Mockito.verify(userRepository).findByEmail("ahmet@gmail.com");
	}
	
	@Test
	public void testUpdateUser_whenUserDoesNotExist_shouldReturnUserNotFoundException() {
		UserUpdateRequest request = UserUpdateRequest.builder()
				.userId(1L)
				.firstName("Çetin")
				.lastName("Mehmet")
				.email("mehmet@gmail.com")
				.password("yalcin99")
				.build();
		
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		assertThrows(UserNotFoundException.class, () -> {
	        userService.updateUser(request);
	    });
		
		Mockito.verify(userRepository).findById(1L);
	}
}
