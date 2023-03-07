package com.todo.api.response;

import com.todo.api.model.Role;
import com.todo.api.model.User;
import com.todo.api.response.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

	private Long id;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private Role role;
    
    public UserResponse(User user) {
    	this.id = user.getId();
    	this.firstName = user.getFirstName();
    	this.lastName = user.getLastName();
    	this.password = user.getPassword();
    	this.email = user.getEmail();
    	this.role = user.getRole();
    }
}
