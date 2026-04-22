# Fitness AI Tracker

Fitness AI Tracker is a full-stack portfolio project built to reduce friction for beginners in bodybuilding and general fitness.

The app helps users decide **what to train next**, **how recovered each muscle group is**, and **how to structure a workout** based on workout duration, training history, and exercise progression. It also supports **manual workout logging** and an **AI-assisted workflow** that tailors generated workouts to a user’s preferences.

---

## What problem this app solves

Beginners often struggle with three things:

- choosing what to train on a given day
- avoiding overtraining muscles that are still fatigued
- knowing how to progress exercises over time

This project addresses that by combining:

- recovery-aware muscle recommendations
- deterministic workout generation based on available time and training state
- exercise progression suggestions based on recent sets
- optional AI tailoring for more natural user requests

---

## Core features

### 1. Recovery-aware training suggestions
The app tracks muscle fatigue and recovery status for each user and suggests which muscle groups are the best candidates for the next workout.

- recovery map per muscle group
- statuses such as **fresh**, **recovering**, and **sore**
- suggestions ranked by lowest current fatigue
- fatigue decay over time

### 2. Deterministic workout generation
Users can generate a workout based on workout duration and current recovery state.

- chooses muscle groups dynamically
- adapts selection to the user’s goal
- picks exercises from the available catalogue
- respects time budget using estimated set duration and rest time
- avoids unnecessary overlap by ranking exercises by target-muscle coverage

### 3. AI-tailored workout generation
Users can optionally describe preferences in natural language, and the app uses Gemini to tailor the generated workout.

Examples:
- “make it more chest-focused”
- “keep it lighter today”
- “I want a shorter arms workout”
- “swap to different muscle groups today”

The AI flow is structured rather than open-ended:
- the backend first generates a deterministic workout
- Gemini classifies the request intent
- Gemini either tweaks the generated workout or assembles a workout from a validated exercise catalogue
- exercise IDs and structure remain grounded in backend data

### 4. Exercise progression support
The app analyzes recent completed sets and recommends how to progress an exercise.

- detects stable vs pyramid training patterns
- recommends increasing reps or weight depending on recent performance
- falls back to baseline loads for cold-start users
- includes progression reasoning in generated workouts

### 5. Manual workout creation and logging
Users are not locked into automation. They can also create and track workouts manually.

- create workouts manually
- add sets to a workout
- mark sets as completed
- view workout history and workout sets
- combine manual logging with recovery tracking

### 6. Authentication and user profile basics
- register and login with JWT-based authentication
- protected API endpoints
- user-specific workouts, recovery data, and recommendations
- support for user goal and experience level

### 7. Simple frontend pages for the full flow
The project includes static frontend pages for:
- registration
- login
- dashboard
- manual workout logging
- workout generation
- recovery overview

### 8. API documentation and health check
- OpenAPI / Swagger UI support through springdoc
- health endpoint for deployment checks

---

## Tech stack

### Backend
- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security**
- **JWT (jjwt)**
- **Bean Validation**
- **Lombok**

### Database
- **PostgreSQL**

### AI integration
- **Google Gemini API**

### Documentation
- **springdoc-openapi / Swagger UI**

### Frontend
- server-served **HTML/CSS/JavaScript** pages from `src/main/resources/static`

---

## Architecture overview

The project is organized in a fairly standard layered Spring Boot structure.

### Request flow
1. A user interacts with the static frontend pages.
2. Requests go to REST controllers under `/api/...`.
3. Controllers delegate business logic to services.
4. Services use algorithm components for recovery, progression, and workout generation.
5. Repositories persist and query user, workout, set, exercise, and recovery data in PostgreSQL.
6. For AI-assisted requests, the backend calls Gemini after generating a deterministic baseline workout.

### Main backend modules
- `controller` — REST API endpoints
- `service` — business logic orchestration
- `algorithm` — recovery, progression, and workout generation logic
- `repository` — JPA persistence layer
- `security` — JWT auth filter and Spring Security config
- `model` / `dto` — domain entities and API payloads

### Key domain concepts
- **User** — profile, goal, experience level
- **Workout** — a logged training session
- **SetEntry** — a performed or planned set
- **ExerciseType** — metadata about an exercise, including primary muscle group and baseline load
- **MuscleRecovery** — current fatigue/recovery state per muscle group

### Design approach
The app intentionally separates:
- **deterministic logic** for consistency and safety
- **AI tailoring** for flexibility and natural-language customization

That makes the AI feature more grounded and easier to control than fully free-form generation.

---

## Features by endpoint area

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`

### Recovery
- `GET /api/recovery`
- `GET /api/recovery/suggest`

### Workout generation
- `POST /api/workout-generation/generate` — deterministic generation
- `POST /api/ai/generate-workout` — AI-tailored generation

### Workouts and sets
- `GET /api/workouts`
- `POST /api/workouts`
- `POST /api/workouts/create_generated_workout`
- `DELETE /api/workouts/{workoutId}/delete`
- `GET /api/workouts/{workoutId}/sets`
- `POST /api/sets/{workoutId}/sets`
- `PATCH /api/sets/{setId}/complete`

### Exercises
- `GET /api/exercises`
- `GET /api/exercises/search`
- `GET /api/exercises/id/{id}`
- `GET /api/exercises/muscle-group/{muscleGroup}`
- `POST /api/exercises`
- `POST /api/exercises/bulk`

### User settings
- `POST /api/users/me/exp`
- `POST /api/users/me/goal`

### Health
- `GET /api/health`

---

## Screenshots / demo


## How to run locally

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- Gemini API key

### 1. Create a PostgreSQL database
Create a database named `fitnessdb`.

Example:

```sql
CREATE DATABASE fitnessdb;
CREATE USER fitness WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE fitnessdb TO fitness;
```

### 2. Set environment variables
The application expects these environment variables:

```bash
export DB_PASSWORD=your_postgres_password
export GEMINI_API_KEY=your_gemini_api_key
```

### 3. Check database configuration
Current application properties expect:

- database: `fitnessdb`
- username: `fitness`
- password from `DB_PASSWORD`
- local PostgreSQL on port `5432`

If needed, update:

`src/main/resources/application.properties`

### 4. Run the application
Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or build and run:

```bash
./mvnw clean package
java -jar target/fitness-ai-tracker-0.0.1-SNAPSHOT.jar
```

### 5. Open the app
Once running, open:

- `http://localhost:8080/login.html`
- `http://localhost:8080/register.html`
- `http://localhost:8080/dashboard.html`
- `http://localhost:8080/generate-workout.html`
- `http://localhost:8080/recovery.html`
- `http://localhost:8080/workout.html`

Health check:

- `http://localhost:8080/api/health`

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

---

## Demo credentials

```txt
Email: demo@fitness.local
Password: demo123
```

---

## Example user flow

1. Register a new account
2. Set training goal and experience level
3. Generate a deterministic workout based on duration
4. Optionally add a natural-language request for AI tailoring
5. Save the generated workout
6. Complete sets during training
7. Review recovery map and next-muscle suggestions
8. Return later and generate the next session with updated recovery/progression context


## Project structure

```text
src/main/java/com/ruslandontsov/fitness
├── algorithm
├── config
├── controller
├── dto
├── exception
├── model
├── repository
├── security
└── service

src/main/resources
├── application.properties
└── static
    ├── dashboard.html
    ├── generate-workout.html
    ├── login.html
    ├── recovery.html
    ├── register.html
    └── workout.html
```

---

## License

No license has been added yet.
If you plan to make the repository public, add a license such as MIT.
