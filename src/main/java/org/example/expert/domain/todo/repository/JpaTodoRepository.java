package org.example.expert.domain.todo.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JpaTodoRepository {
    Page<Todo> findTodos(Pageable pageable, String weather, LocalDateTime StartDate, LocalDateTime endDate);

    Optional<Todo> findByIdWithUser(Long todoId);
}
