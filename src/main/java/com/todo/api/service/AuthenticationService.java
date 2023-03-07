package com.todo.api.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.todo.api.config.JwtService;
import com.todo.api.exception.CustomBadCredentialsException;
import com.todo.api.exception.NotUniqueEmailException;
import com.todo.api.exception.UserNotFoundException;
import com.todo.api.model.User;
import com.todo.api.repository.UserRepository;
import com.todo.api.request.AuthenticationRequest;
import com.todo.api.request.RegisterRequest;
import com.todo.api.response.AuthenticationResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	
	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		Optional<User> userObj = repository.findByEmail(request.getEmail());
		if(userObj.isPresent()) {
			throw new NotUniqueEmailException("There is a user in the system with this email address");
		}
		else {
			var user = User.builder()
					.firstName(request.getFirstName())
					.lastName(request.getLastName())
					.email(request.getEmail())
					.password(passwordEncoder.encode(request.getPassword()))
					.role(request.getRole())
					.build();
			repository.save(user);
			var jwtToken = jwtService.generateToken(user);
			return AuthenticationResponse.builder()
					.token(jwtToken)
					.build();
		}
	}
	
	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		return repository.findByEmail(request.getEmail())
		        .map(user -> {
		            try {
		                authenticationManager.authenticate(
		                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		                var jwtToken = jwtService.generateToken(user);
		                return AuthenticationResponse.builder()
		                    .token(jwtToken)
		                    .userId(user.getId())
		                    .build();
		            } catch (AuthenticationException ex) {
		                throw new CustomBadCredentialsException("Invalid email or password.");
		            }})
		        .orElseThrow(() -> new UserNotFoundException("User not found."));
	}

}

