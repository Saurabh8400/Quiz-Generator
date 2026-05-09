-- ============================================================
-- AI Quiz Generator - MySQL Database Schema
-- ============================================================
-- Run this manually if you prefer explicit schema control.
-- Otherwise, Spring Boot with ddl-auto=update creates tables automatically.
-- ============================================================

CREATE DATABASE IF NOT EXISTS ai_quiz_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ai_quiz_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100),
    role         VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Quizzes table
CREATE TABLE IF NOT EXISTS quizzes (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(200) NOT NULL,
    description        TEXT,
    topic              VARCHAR(100) NOT NULL,
    difficulty         VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    num_questions      INT          NOT NULL DEFAULT 10,
    time_limit_minutes INT,
    is_public          BOOLEAN      NOT NULL DEFAULT FALSE,
    llm_model          VARCHAR(50),
    prompt_used        TEXT,
    user_id            BIGINT       NOT NULL,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_quizzes_user (user_id),
    INDEX idx_quizzes_topic (topic),
    INDEX idx_quizzes_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT        NOT NULL,
    type          VARCHAR(20) NOT NULL DEFAULT 'MCQ',
    correct_answer TEXT       NOT NULL,
    points        INT         NOT NULL DEFAULT 1,
    explanation   TEXT,
    order_index   INT,
    quiz_id       BIGINT      NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_questions_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Options table (MCQ choices)
CREATE TABLE IF NOT EXISTS options (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    option_text  TEXT        NOT NULL,
    option_label VARCHAR(5),
    is_correct   BOOLEAN     NOT NULL DEFAULT FALSE,
    question_id  BIGINT      NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_options_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User attempts table
CREATE TABLE IF NOT EXISTS user_attempts (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT         NOT NULL,
    quiz_id            BIGINT         NOT NULL,
    score              DOUBLE,
    total_points       INT,
    earned_points      INT,
    percentage         DOUBLE,
    status             VARCHAR(20)    NOT NULL DEFAULT 'IN_PROGRESS',
    time_taken_seconds INT,
    started_at         DATETIME,
    completed_at       DATETIME,
    created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    INDEX idx_attempts_user (user_id),
    INDEX idx_attempts_quiz (quiz_id),
    INDEX idx_attempts_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Attempt answers table
CREATE TABLE IF NOT EXISTS attempt_answers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id      BIGINT  NOT NULL,
    question_id     BIGINT  NOT NULL,
    selected_answer TEXT,
    is_correct      BOOLEAN NOT NULL DEFAULT FALSE,
    points_earned   INT     NOT NULL DEFAULT 0,
    FOREIGN KEY (attempt_id)  REFERENCES user_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)     ON DELETE CASCADE,
    INDEX idx_answers_attempt (attempt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
