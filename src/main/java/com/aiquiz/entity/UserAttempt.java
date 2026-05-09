package com.aiquiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_attempts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "score")
    private Double score;

    @Column(name = "total_points")
    private Integer totalPoints;

    @Column(name = "earned_points")
    private Integer earnedPoints;

    @Column(name = "percentage")
    private Double percentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<AttemptAnswer> answers = new ArrayList<>();

    public enum AttemptStatus {
        IN_PROGRESS, COMPLETED, ABANDONED
    }
}
