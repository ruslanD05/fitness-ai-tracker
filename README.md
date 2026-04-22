# Fitness AI Tracker

Fitness AI Tracker is a full-stack portfolio project designed to reduce friction for beginners in bodybuilding and fitness.

It helps users decide what to train next, track muscle recovery, generate workouts based on recovery state and available time, and log workouts manually. It also supports AI-assisted workout tailoring using Gemini.

---

## Core features

- **Recovery-aware training suggestions**  
  Tracks fatigue and recovery per muscle group and recommends what to train next.

- **Deterministic workout generation**  
  Builds workouts based on available time, recovery state, and exercise coverage.

- **AI-tailored generation**  
  Lets users refine generated workouts with natural-language preferences while keeping exercise selection grounded in backend data.

- **Exercise progression support**  
  Uses recent set history to suggest progression in reps or weight.

- **Manual workout logging**  
  Users can create workouts manually, add sets, mark them complete, and review workout history.

- **Authentication and user profiles**  
  JWT-based auth with user-specific recovery data, workouts, and recommendations.

---

## Tech stack

- **Backend:** Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, JWT, Bean Validation
- **Database:** PostgreSQL
- **AI:** Google Gemini API
- **Docs:** springdoc-openapi / Swagger UI
- **Frontend:** static HTML/CSS/JavaScript served by Spring Boot

---

## Architecture

The app follows a layered Spring Boot structure:

- `controller` — REST endpoints
- `service` — business logic
- `algorithm` — recovery, progression, and workout generation
- `repository` — persistence layer
- `security` — JWT auth and Spring Security config
- `model` / `dto` — entities and API payloads

The design separates deterministic workout logic from AI-based tailoring so the AI remains grounded and controlled.

---

## How to run

### Prerequisites
- Docker
- Docker Compose
- Gemini API key

### Setup and start

```bash
cd fitness-ai-tracker
cp .env.example .env
# enter your API key and other values in .env
nano .env
docker compose --env-file .env up --build
