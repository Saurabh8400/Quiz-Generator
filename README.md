<div align="center">

<img src="https://img.shields.io/badge/AI-Powered-blueviolet?style=for-the-badge&logo=openai&logoColor=white"/>
<img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
<img src="https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
<img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge"/>

<br/><br/>

# 🤖 AI Quiz Generator

### *Instantly generate smart, structured quizzes on any topic using AI — powered by LLM APIs, secured with JWT, and built on Spring Boot.*

<br/>

[🚀 Quick Start](#-quick-start) &nbsp;·&nbsp;
[📡 API Docs](#-api-endpoints) &nbsp;·&nbsp;
[🧱 Architecture](#-architecture) &nbsp;·&nbsp;
[🛠️ Tech Stack](#%EF%B8%8F-tech-stack) &nbsp;·&nbsp;
[🤝 Contributing](#-contributing)

</div>

---

## 📌 About The Project

**AI Quiz Generator** is a full-stack backend system that uses **Large Language Models (LLMs)** to dynamically generate quizzes on any topic. A user simply provides a topic, difficulty level, and number of questions — and the system calls the OpenAI API, parses the AI response into structured JSON, persists it in MySQL, and returns a fully ready quiz with options and explanations.

The project demonstrates real-world skills in:
- **Prompt Engineering** — crafting system & user prompts that force the LLM to return consistent, parseable JSON
- **REST API Design** — clean, versioned endpoints with proper HTTP semantics
- **Security** — stateless JWT authentication, BCrypt password hashing, role-based access
- **Database Design** — normalized MySQL schema optimized for quizzes, attempts, and analytics
- **Resilient Parsing** — defensive LLM response parser that handles markdown fences, null fields, and partial failures

---

## ✨ Features

| Feature | Description |
|--------|-------------|
| 🤖 **AI Quiz Generation** | Generates MCQ & True/False questions via OpenAI GPT |
| 🔐 **JWT Authentication** | Secure register/login with stateless Bearer tokens |
| 📝 **Prompt Engineering** | Structured prompts ensure consistent LLM JSON output |
| 🧩 **Response Parsing** | Robust parser handles edge cases in LLM responses |
| 📊 **Performance Analytics** | Tracks scores, attempt history, topics explored |
| 🗄️ **Optimized Schema** | Normalized MySQL tables with proper indexes |
| 🌐 **Public Quiz Browse** | Search and discover quizzes by other users |
| 🧪 **Unit Tested** | JUnit 5 tests with H2 in-memory DB (no MySQL needed) |

---

## 🛠️ Tech Stack

<div align="center">

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.0 |
| Security | Spring Security + JJWT | 0.11.5 |
| Database | MySQL | 8.0 |
| ORM | Spring Data JPA (Hibernate) | — |
| HTTP Client | Spring WebFlux WebClient | — |
| AI / LLM | OpenAI GPT API | gpt-3.5-turbo / gpt-4 |
| Build Tool | Maven | 3.8+ |
| Testing | JUnit 5 + H2 | — |
| Password Hashing | BCrypt | — |
| JSON | Jackson | — |

</div>

---

## 🧱 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT / POSTMAN                      │
└──────────────────────┬──────────────────────────────────┘
                       │  HTTP + JWT
┌──────────────────────▼──────────────────────────────────┐
│               SPRING BOOT APPLICATION                    │
│                                                          │
│  ┌─────────────┐   ┌──────────────┐   ┌─────────────┐  │
│  │ Controllers │──▶│   Services   │──▶│Repositories │  │
│  │  /api/auth  │   │ AuthService  │   │  UserRepo   │  │
│  │  /api/quiz  │   │ QuizService  │   │  QuizRepo   │  │
│  │ /api/attempt│   │AttemptService│   │ AttemptRepo │  │
│  │/api/analytics│  │AnalyticsServ │   │             │  │
│  └─────────────┘   └──────┬───────┘   └──────┬──────┘  │
│                           │                   │         │
│              ┌────────────▼──────┐      ┌─────▼──────┐  │
│              │   LLM Service     │      │   MySQL    │  │
│              │  PromptBuilder    │      │  Database  │  │
│              │  ResponseParser   │      └────────────┘  │
│              └────────────┬──────┘                      │
└───────────────────────────┼─────────────────────────────┘
                            │  HTTPS
                ┌───────────▼───────────┐
                │    OpenAI GPT API     │
                │  (or any LLM API)     │
                └───────────────────────┘
```

### Database Schema (ER Diagram)

```
users
  │
  ├──< quizzes ──< questions ──< options
  │         │
  └──< user_attempts ──< attempt_answers
```

**Tables:** `users` · `quizzes` · `questions` · `options` · `user_attempts` · `attempt_answers`

---

## 📁 Project Structure

```
ai-quiz-generator/
├── src/main/java/com/aiquiz/
│   ├── AiQuizGeneratorApplication.java    # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java            # Spring Security + JWT setup
│   │   ├── WebClientConfig.java           # WebClient for OpenAI API
│   │   ├── JacksonConfig.java             # ObjectMapper config
│   │   └── GlobalExceptionHandler.java    # Centralized error handling
│   ├── controller/
│   │   ├── AuthController.java            # Register & Login
│   │   ├── QuizController.java            # Quiz CRUD + AI generation
│   │   ├── AttemptController.java         # Submit quiz, get results
│   │   └── AnalyticsController.java       # User stats & analytics
│   ├── dto/                               # Request/Response objects
│   ├── entity/                            # JPA entities
│   ├── repository/                        # Spring Data JPA repos
│   ├── security/
│   │   ├── JwtUtil.java                   # Token creation & validation
│   │   ├── JwtAuthenticationFilter.java   # Per-request JWT filter
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   ├── service/
│   │   ├── LlmService.java                # OpenAI API integration
│   │   ├── QuizService.java               # Quiz generation + management
│   │   ├── AttemptService.java            # Answer scoring
│   │   ├── AuthService.java               # Auth logic
│   │   └── AnalyticsService.java          # Performance stats
│   └── util/
│       ├── PromptBuilder.java             # Prompt engineering
│       └── LlmResponseParser.java         # JSON response parser
├── src/test/java/com/aiquiz/
│   ├── AiQuizGeneratorApplicationTest.java
│   ├── AuthServiceTest.java
│   ├── JwtUtilTest.java
│   ├── LlmResponseParserTest.java
│   └── PromptBuilderTest.java
├── schema.sql                             # MySQL schema (optional)
├── pom.xml
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites

- ☕ Java 17+
- 📦 Maven 3.8+
- 🐬 MySQL 8.0+
- 🔑 OpenAI API Key → [Get one here](https://platform.openai.com/api-keys)

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/ai-quiz-generator.git
cd ai-quiz-generator
```

### 2. Configure the Application

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ai_quiz_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# JWT Secret (use a long random string)
jwt.secret=your_super_secret_key_min_32_characters_long
jwt.expiration=86400000

# OpenAI API
llm.api.key=sk-your-openai-api-key-here
llm.api.model=gpt-3.5-turbo
```

### 3. Run

```bash
mvn spring-boot:run
```

> ✅ MySQL tables are **auto-created** on first startup via `spring.jpa.hibernate.ddl-auto=update`
>
> Or manually run `schema.sql` in MySQL Workbench / CLI.

The server starts at: **`http://localhost:8080`**

---

## 📡 API Endpoints

### 🔐 Auth (Public — No Token Needed)

```
POST   /api/auth/register     →  Register a new user
POST   /api/auth/login        →  Login and receive JWT token
```

### 🧠 Quizzes (Requires: `Authorization: Bearer <token>`)

```
POST   /api/quizzes/generate            →  Generate AI quiz
GET    /api/quizzes/{id}                →  Get quiz by ID
GET    /api/quizzes/my?page=0&size=10   →  My quizzes (paginated)
GET    /api/quizzes/public              →  Browse public quizzes
GET    /api/quizzes/public/search?keyword=java  →  Search quizzes
DELETE /api/quizzes/{id}                →  Delete a quiz
```

### 📝 Attempts (Requires Auth)

```
POST   /api/attempts/submit             →  Submit quiz answers
GET    /api/attempts/{id}               →  Get attempt result
GET    /api/attempts/my?page=0&size=10  →  My attempt history
```

### 📊 Analytics (Requires Auth)

```
GET    /api/analytics/me                →  My performance analytics
```

---

## 📬 Sample API Calls

<details>
<summary><b>📥 Register a new user</b></summary>

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rahul",
    "email": "rahul@example.com",
    "password": "secret123",
    "fullName": "Rahul Sharma"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "rahul",
    "role": "ROLE_USER"
  }
}
```
</details>

<details>
<summary><b>🤖 Generate an AI Quiz</b></summary>

```bash
curl -X POST http://localhost:8080/api/quizzes/generate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Java Spring Boot",
    "numQuestions": 5,
    "difficulty": "MEDIUM",
    "title": "Spring Boot Interview Prep",
    "isPublic": true,
    "additionalInstructions": "Focus on REST APIs and annotations"
  }'
```

**Difficulty options:** `EASY` · `MEDIUM` · `HARD` · `EXPERT`
</details>

<details>
<summary><b>📝 Submit Quiz Answers</b></summary>

```bash
curl -X POST http://localhost:8080/api/attempts/submit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": 1,
    "timeTakenSeconds": 240,
    "answers": [
      { "questionId": 1, "selectedAnswer": "A" },
      { "questionId": 2, "selectedAnswer": "True" },
      { "questionId": 3, "selectedAnswer": "C" }
    ]
  }'
```
</details>

<details>
<summary><b>📊 Get My Analytics</b></summary>

```bash
curl -X GET http://localhost:8080/api/analytics/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalQuizzesCreated": 12,
    "totalAttemptsCompleted": 34,
    "averageScore": 78.5,
    "highestScore": 100.0,
    "topicsExplored": ["Java", "Spring Boot", "MySQL", "DSA"]
  }
}
```
</details>

---

## 🔄 How AI Quiz Generation Works

```
User Request
     │
     ▼
PromptBuilder.buildSystemPrompt()   ← Instructs LLM to return strict JSON only
PromptBuilder.buildUserPrompt()     ← Topic + difficulty + question count + schema
     │
     ▼
LlmService → POST https://api.openai.com/v1/chat/completions
     │
     ▼
LlmResponseParser.parse()
  ├─ Strip markdown fences (```json ... ```)
  ├─ Parse JSON into LlmQuestionDto[]
  ├─ Skip malformed/empty questions
  └─ Return clean List<LlmQuestionDto>
     │
     ▼
QuizService → Map to JPA entities → Save to MySQL
     │
     ▼
Return QuizResponse (correct answers hidden)
```

---

## 🧪 Running Tests

Tests use **H2 in-memory database** — no MySQL required.

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=LlmResponseParserTest
mvn test -Dtest=JwtUtilTest
mvn test -Dtest=PromptBuilderTest
```

**Test Coverage:**
- ✅ User registration & duplicate validation
- ✅ Login with username or email
- ✅ JWT token generation & validation
- ✅ LLM response parsing (clean JSON, markdown fences, malformed inputs)
- ✅ Prompt builder content verification
- ✅ Spring context load

---

## 🔧 Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `jwt.expiration` | `86400000` | Token validity in ms (24h) |
| `llm.api.model` | `gpt-3.5-turbo` | LLM model to use |
| `llm.api.max-tokens` | `2000` | Max tokens per LLM response |
| `llm.api.temperature` | `0.7` | Creativity level (0.0–1.0) |
| `llm.api.timeout-seconds` | `60` | API call timeout |

### Using a Different LLM (e.g., GPT-4, Ollama, Azure)

```properties
# GPT-4
llm.api.model=gpt-4

# Ollama (local)
llm.api.url=http://localhost:11434/v1/chat/completions
llm.api.key=ollama
llm.api.model=llama3

# Azure OpenAI
llm.api.url=https://YOUR_RESOURCE.openai.azure.com/openai/deployments/YOUR_DEPLOYMENT/chat/completions?api-version=2024-02-01
```

---

## 🔐 Security Design

- **Passwords** hashed with **BCrypt** (never stored as plain text)
- **JWT tokens** signed with HMAC-SHA256, expire in 24 hours
- **Correct answers** are **never returned** in quiz fetch APIs — only revealed after submission
- **CORS** enabled for all origins in dev — restrict to your frontend domain in production
- **Role-based access**: `ROLE_USER` and `ROLE_ADMIN` supported

---

## 🤝 Contributing

Contributions are what make the open-source community amazing. Any contribution you make is **greatly appreciated**!

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/AmazingFeature`
3. Commit your changes: `git commit -m 'Add: AmazingFeature'`
4. Push to the branch: `git push origin feature/AmazingFeature`
5. Open a **Pull Request**

**Ideas for contribution:**
- Add support for image-based questions
- Add leaderboard / ranking system
- Add export quiz to PDF feature
- Add email notifications
- Frontend (React / Angular)

---

## 📋 Roadmap

- [x] AI quiz generation via LLM API
- [x] JWT authentication
- [x] Quiz attempt & scoring
- [x] User analytics
- [x] Public quiz search
- [ ] Rate limiting per user
- [ ] Quiz categories & tags
- [ ] Email verification on signup
- [ ] Admin dashboard
- [ ] Frontend (React)
- [ ] Docker & docker-compose support

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for more information.

---

## 👨‍💻 Author

**Your Name**
- GitHub: [@Saurabh8400](https://github.com/Saurabh8400)
- LinkedIn: [Saurabh Kumar Pandey](https://www.linkedin.com/in/saurabh-1a6aa2195/)
- Email: saurabh.pandey222000@gmail.com

---

## 🙏 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [OpenAI API](https://platform.openai.com/)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [Shields.io](https://shields.io/) — for the badges
- [othneildrew/Best-README-Template](https://github.com/othneildrew/Best-README-Template)

---

<div align="center">

⭐ **If this project helped you, please give it a star!** ⭐

Made with ❤️ and ☕ in Java

</div>
