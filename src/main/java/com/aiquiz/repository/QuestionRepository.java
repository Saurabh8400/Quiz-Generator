package com.aiquiz.repository;

import com.aiquiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByOrderIndex(Long quizId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId")
    long countByQuizId(@Param("quizId") Long quizId);
}
