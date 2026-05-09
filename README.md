# 🤖 AI Quiz Generator

An AI-powered quiz generation system built with **Java**, **Spring Boot**, **MySQL**, and **LLM APIs (OpenAI)**. Generates dynamic, structured quizzes using prompt engineering, secured via JWT authentication.

---

## 📋 Features

- **AI Quiz Generation** — Calls OpenAI GPT API to generate topic-based MCQ and True/False questions
- **JWT Authentication** — Secure registration, login, and protected REST APIs
- **Prompt Engineering** — Structured system/user prompts ensuring consistent JSON output from LLM
- **Response Parsing** — Robust parser handles markdown fences, missing fields, and malformed LLM output
- **Quiz Management** — Create, retrieve, search, and delete quizzes
- **Attempt Tracking** — Submit quiz answers, get scored results with explanations
- **Performance Analytics** — Track average scores, highest scores, topics explored
- **MySQL Database** — Optimized schema for quizzes, questions, options, attempts, and answers

---

## 🏗️ Project Structure

```
ai-quiz-generator/
├── src/main/java/com/aiquiz/
│   ├── AiQuizGeneratorApplication.java   # Spring Boot entry point
│   ├── config/
│   │   ├── SecurityConfig.java           # JWT + Spring Security config
│   │   ├── WebClientConfig.java          # WebClient for LLM API calls
│   │   ├── JacksonConfig.java            # ObjectMapper configuration
│   │   └── GlobalExceptionHandler.java   # Centralized error handling
│   ├── controller/
│   │   ├── AuthController.java           # POST /api/auth/register, /login
│   │   ├── QuizController.java           # Quiz CRUD + AI generation
│   │   ├── AttemptController.java        # Submit attempts, get results
│   │   └── AnalyticsController.java      # User performance analytics
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   ├── QuizGenerateRequest.java
│   │   ├── QuizResponse.java
│   │   ├── SubmitAttemptRequest.java
│   │   ├── AttemptResultResponse.java
│   │   ├── UserAnalyticsResponse.java
│   │   ├── LlmQuestionDto.java           # Internal DTO for LLM parsing
│   │   └── ApiResponse.java              # Generic API wrapper
│   ├── entity/
│   │   ├── User.java
│   │   ├── Quiz.java
│   │   ├── Question.java
│   │   ├── Option.java
│   │   ├── UserAttempt.java
│   │   └── AttemptAnswer.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── QuizRepository.java
│   │   ├── QuestionRepository.java
│   │   └── UserAttemptRepository.java
│   ├── security/
│   │   ├── JwtUtil.java                  # Token generation & validation
│   │   ├── JwtAuthenticationFilter.java  # Per-request JWT filter
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   ├── service/
│   │   ├── AuthService.java              # Registration & login logic
│   │   ├── LlmService.java               # OpenAI API integration
│   │   ├── QuizService.java              # Quiz generation & management
│   │   ├── AttemptService.java           # Attempt scoring
│   │   └── AnalyticsService.java         # User analytics
│   └── util/
│       ├── PromptBuilder.java            # Prompt engineering
│       └── LlmResponseParser.java        # JSON response parser
├── src/test/java/com/aiquiz/
│   ├── AuthServiceTest.java
│   ├── JwtUtilTest.java
│   ├── LlmResponseParserTest.java
│   └── PromptBuilderTest.java
├── schema.sql                            # MySQL schema (optional manual setup)
└── pom.xml
```

---

## ⚙️ Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **OpenAI API Key** (or compatible LLM API)

---

## 🚀 Setup & Run

### 1. Clone & Configure

```bash
git clone <repo-url>
cd ai-quiz-generator
```

### 2. Configure `application.properties`

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ai_quiz_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# JWT
jwt.secret=your_very_long_secret_key_at_least_32_chars

# OpenAI
llm.api.key=sk-your-openai-api-key-here
llm.api.model=gpt-3.5-turbo
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The server starts at: `http://localhost:8080`

> The database and tables are created automatically via `spring.jpa.hibernate.ddl-auto=update`.  
> Or manually run `schema.sql` in your MySQL client.

---

## 🔑 API Endpoints

### Auth (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, receive JWT token |

### Quizzes (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/quizzes/generate` | Generate AI quiz |
| GET | `/api/quizzes/{id}` | Get quiz by ID |
| GET | `/api/quizzes/my` | Get my quizzes (paginated) |
| GET | `/api/quizzes/public` | Browse public quizzes |
| GET | `/api/quizzes/public/search?keyword=java` | Search public quizzes |
| DELETE | `/api/quizzes/{id}` | Delete a quiz |

### Attempts (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attempts/submit` | Submit quiz answers |
| GET | `/api/attempts/{id}` | Get attempt result |
| GET | `/api/attempts/my` | Get my attempt history |

### Analytics (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/me` | Get my performance analytics |

---

## 📬 Example API Requests

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "secret123",
    "fullName": "John Doe"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john",
    "password": "secret123"
  }'
```

### Generate AI Quiz
```bash
curl -X POST http://localhost:8080/api/quizzes/generate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Java Spring Boot",
    "numQuestions": 5,
    "difficulty": "MEDIUM",
    "title": "Spring Boot Basics",
    "isPublic": true,
    "additionalInstructions": "Focus on REST APIs and dependency injection"
  }'
```

### Submit Attempt
```bash
curl -X POST http://localhost:8080/api/attempts/submit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": 1,
    "timeTakenSeconds": 120,
    "answers": [
      {"questionId": 1, "selectedAnswer": "A"},
      {"questionId": 2, "selectedAnswer": "True"},
      {"questionId": 3, "selectedAnswer": "C"}
    ]
  }'
```

---

## 🧪 Running Tests

```bash
# All tests (uses H2 in-memory database — no MySQL needed)
mvn test

# Specific test class
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=LlmResponseParserTest
```

---

## 🗄️ Database Schema (ER Overview)

```
users ──< quizzes ──< questions ──< options
  │            │
  └──< user_attempts ──< attempt_answers
```

---

## 🔧 LLM Configuration

The system works with any OpenAI-compatible API. To use a different model:

```properties
llm.api.model=gpt-4
llm.api.max-tokens=3000
llm.api.temperature=0.5
```

To use a self-hosted or alternative LLM (e.g., Ollama, Azure OpenAI):
```properties
llm.api.url=http://localhost:11434/v1/chat/completions
llm.api.key=ollama
llm.api.model=llama3
```

---

## 🔐 Security Notes

- Passwords are hashed with **BCrypt**
- JWT tokens expire in **24 hours** (configurable via `jwt.expiration`)
- CORS is enabled for all origins in dev — restrict in production
- Correct answers are **never exposed** in quiz fetch APIs — only revealed after attempt submission

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database | MySQL 8 + Spring Data JPA (Hibernate) |
| HTTP Client | Spring WebFlux WebClient |
| LLM API | OpenAI GPT (gpt-3.5-turbo / gpt-4) |
| Build | Maven |
| Testing | JUnit 5, H2 in-memory DB |
