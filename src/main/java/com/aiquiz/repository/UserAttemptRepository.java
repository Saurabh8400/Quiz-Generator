package com.aiquiz.repository;

import com.aiquiz.entity.UserAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAttemptRepository extends JpaRepository<UserAttempt, Long> {

    Page<UserAttempt> findByUserId(Long userId, Pageable pageable);

    List<UserAttempt> findByUserIdAndQuizId(Long userId, Long quizId);

    Optional<UserAttempt> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(a) FROM UserAttempt a WHERE a.user.id = :userId AND a.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(a.percentage) FROM UserAttempt a WHERE a.user.id = :userId AND a.status = 'COMPLETED'")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(a.percentage) FROM UserAttempt a WHERE a.user.id = :userId AND a.status = 'COMPLETED'")
    Double getHighestScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM UserAttempt a WHERE a.user.id = :userId AND a.status = 'COMPLETED' ORDER BY a.completedAt DESC")
    List<UserAttempt> findRecentCompletedByUserId(@Param("userId") Long userId, Pageable pageable);
}
