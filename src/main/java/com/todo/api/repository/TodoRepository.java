package com.todo.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todo.api.model.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
	List<Todo> findByUserId(Long userId);
	List<Todo> findByActiveAndUserId(boolean active, Long userId);
}
