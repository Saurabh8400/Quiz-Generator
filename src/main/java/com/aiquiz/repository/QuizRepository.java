package com.aiquiz.repository;

import com.aiquiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findByUserId(Long userId, Pageable pageable);

    Page<Quiz> findByIsPublicTrue(Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND " +
           "(LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.topic) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Quiz> searchPublicQuizzes(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT q.topic FROM Quiz q WHERE q.user.id = :userId")
    List<String> findDistinctTopicsByUserId(@Param("userId") Long userId);
}
