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

import com.todo.api.request.RegisterRequest;
import com.todo.api.request.UserUpdateRequest;
import com.todo.api.response.UserResponse;
import com.todo.api.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	
	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers(){
		return ResponseEntity.ok(userService.getAllUsers());
	}
	
	@GetMapping("/{email}")
	public ResponseEntity<UserResponse> findUserByEmail(@PathVariable String email){
		return ResponseEntity.ok(userService.findUserByEmail(email));
	}
	
	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request){
		return ResponseEntity.ok(userService.createUser(request));
	}
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id){
		userService.deleteUser(id);
		return ResponseEntity.ok().build();
	}
	
	@PutMapping
	public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserUpdateRequest updateRequest){
		return ResponseEntity.ok(userService.updateUser(updateRequest));
	}
}