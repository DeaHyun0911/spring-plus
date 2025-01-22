package org.example.expert.domain.todo.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Repository
public class JpqlTodoRepositoryImpl implements JpqlTodoRepository {

    private final EntityManager em;

    @Override
    public Page<Todo> findTodos(Pageable pageable, String weather, LocalDateTime startDate,
        LocalDateTime endDate) {
        // 기본 목록 조회
        String jpql = "SELECT t FROM Todo t LEFT JOIN FETCH t.user u";
        List<String> conds = new ArrayList<>();

        // 조건 추가
        if (StringUtils.hasText(weather)) {
            conds.add("t.weather LIKE CONCAT('%', :weather, '%')");
        }
        if (startDate != null && endDate != null) {
            conds.add("t.modifiedAt BETWEEN :startDate AND :endDate");
        } else if (startDate != null) {
            conds.add("t.modifiedAt >= :startDate");
        } else if (endDate != null) {
            conds.add("t.modifiedAt <= :endDate");
        }

        if (!conds.isEmpty()) {
            jpql += " WHERE " + String.join(" AND ", conds);
        }

        jpql += " ORDER BY t.modifiedAt DESC";

        TypedQuery<Todo> query = em.createQuery(jpql, Todo.class);
        if (StringUtils.hasText(weather)) {
            query.setParameter("weather", weather);
        }
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Todo> todos = query.getResultList();

        String countJpql = "SELECT COUNT(t) FROM Todo t";
        if (!conds.isEmpty()) {
            countJpql += " WHERE " + String.join(" AND ", conds);
        }
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);

        // COUNT 파라미터 바인딩
        if (weather != null && !weather.isEmpty()) {
            countQuery.setParameter("weather", weather);
        }
        if (startDate != null) {
            countQuery.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            countQuery.setParameter("endDate", endDate);
        }

        long total = countQuery.getSingleResult();

        // 결과 반환
        return new PageImpl<>(todos, pageable, total);

    }
}
