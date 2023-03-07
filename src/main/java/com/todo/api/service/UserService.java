package com.todo.api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.todo.api.exception.NotUniqueEmailException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.User;
import com.todo.api.repository.UserRepository;
import com.todo.api.request.RegisterRequest;
import com.todo.api.request.UserUpdateRequest;
import com.todo.api.response.UserResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	
	public List<UserResponse> getAllUsers(){
		List<User> users = userRepository.findAll();
		List<UserResponse> usersResponse = users.stream().map(user -> new UserResponse(user)).collect(Collectors.toList());
		return usersResponse;
	}
	
	@Transactional
	public UserResponse createUser(RegisterRequest request) {
		Optional<User> userObj =  userRepository.findByEmail(request.getEmail());
		if(userObj.isPresent()) {
			throw new NotUniqueEmailException("There is a user in the system with this email address");
		}
		User user = new User();
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		String encodedPassword = new BCryptPasswordEncoder().encode(request.getPassword());
		user.setPassword(encodedPassword);
		user.setRole(request.getRole());
		userRepository.save(user);
		UserResponse response = new UserResponse(user);
		return response;
	}
	
	@Transactional
	public void deleteUser(Long id) {
		Optional<User> user = userRepository.findById(id);
		if(user.isPresent()) {
			userRepository.deleteById(id);
		}
		else {
			throw new UserNotFoundException("User Not Found!");
		}
	}
	
	
	public UserResponse findUserByEmail(String email) {
		Optional<User> user = userRepository.findByEmail(email);
		if(user.isPresent()) {
			UserResponse response = new UserResponse(user.get());
			return response;
		}
		else {
			throw new UserNotFoundException("User Not Found!");
		}
	}
	
	@Transactional
	public UserResponse updateUser(UserUpdateRequest request) {
		Optional<User> user = userRepository.findById(request.getUserId());
		if(user.isPresent()) {
			User userObj =user.get();
			userObj.setFirstName(request.getFirstName());
			userObj.setLastName(request.getLastName());
			String encodedPassword = new BCryptPasswordEncoder().encode(request.getPassword());
			userObj.setPassword(encodedPassword);
			
			Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
	        if (existingUser.isPresent() && !existingUser.get().getEmail().equals(userObj.getEmail())) {
	            throw new NotUniqueEmailException("There is a user in the system with this email address");
	        } else {
	            userObj.setEmail(request.getEmail());
	        }
	        
			userRepository.save(userObj);
			UserResponse response  = new UserResponse(userObj);
			return response;
		}
		else {
			throw new UserNotFoundException("User Not Found!");
		}
	}

}
