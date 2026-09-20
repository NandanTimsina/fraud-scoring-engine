# Fraud Scoring Engine

A standalone B2B trust and fraud scoring service. Marketplace platforms (think OLX or Facebook Marketplace) send in behavioral events about their users — account creation, listings posted, complaints filed — and this service computes an explainable risk score on demand, backed by a configurable, data-driven rules engine.

This is infrastructure, not an application. It doesn't have a UI or end-users of its own; other backend systems call it as a service.

## How it works

A marketplace platform registers as a client and receives an API key. The platform sends behavioral events for its users as they happen (`POST /events`). When the platform wants a risk assessment, it calls `GET /score/{userId}`. The service pulls that user's event history, runs it through a set of weighted rules, and returns a numeric score along with exactly which rules fired and why.

Every computed score is saved as a permanent, timestamped record — an audit trail, not just a live number.

## Core design decisions

### Multi-tenant by design

All clients' events live in a single shared table, but every row is scoped by `client_id`. A user ID is only meaningful in combination with the client it belongs to — two different platforms can each have a `user_123`, and they are treated as two entirely different people. Every query respects this.

### No PII, ever

The service only ever sees a pseudonymous user ID, an event type, a timestamp, and optional metadata supplied by the client. It was designed from the start to never need or store personal information.

### Rules as data, not code

Scoring logic is not a chain of hardcoded if statements. Each rule is an object with a name, a weight, and a condition function, and all rules live together in one place. Adding, removing, or reweighting a rule is a one-line change, not a refactor.

### Explainability over a black-box number

The API never returns a bare score. It always returns the score alongside the specific rules that triggered and their individual weights, so a caller (or a human reviewing a decision) can see exactly why a user was flagged.

### B2B API-key auth, not user login

Clients of this service are other backend platforms, not people. There is no login form or session — a client authenticates every request with a static API key, verified against a bcrypt hash. Nothing is ever stored or returned in plaintext after the initial registration.

## Tech stack

* Java 17 / Spring Boot — REST API, dependency injection
* PostgreSQL — primary datastore, with JSONB for flexible event metadata
* Spring Data JPA / Hibernate — ORM layer
* Spring Security — API key authentication filter chain
* BCrypt — one-way hashing for API keys
* Docker Compose — local Postgres, no native install required
* Redis (in progress) — cache-aside pattern for score lookups
* AWS SQS (planned) — asynchronous event ingestion
* Lombok — boilerplate reduction

## API overview

| Method | Endpoint            | Auth required     | Description                                                 |
| ------ | ------------------- | ----------------- | ----------------------------------------------------------- |
| `POST` | `/clients/register` | No                | Registers a new client platform, returns a one-time API key |
| `POST` | `/events`           | Yes (`X-API-Key`) | Records a behavioral event for a user                       |
| `GET`  | `/score/{userId}`   | Yes (`X-API-Key`) | Computes and returns the user's current risk score          |

All authenticated endpoints derive the calling client from the API key itself — a client can never see or affect another client's data, even if it somehow obtained another client's internal ID.

## Running locally

Prerequisites: Java 17+, Maven, Docker Desktop.

### 1. Start Postgres

```bash
docker-compose up -d
```

### 2. Run the app

```bash
./mvnw spring-boot:run
```

The API is now available at http://localhost:8080.

## Example usage

### Register a client

```bash
curl -X POST http://localhost:8080/clients/register \
-H "Content-Type: application/json" \
-d '{"name": "Example Marketplace"}'
```

### Send an event

Using the API key from the response above:

```bash
curl -X POST http://localhost:8080/events \
-H "Content-Type: application/json" \
-H "X-API-Key: <your-api-key>" \
-d '{"userId": "user_123", "type": "account_created"}'
```

### Get a score

```bash
curl http://localhost:8080/score/user_123 \
-H "X-API-Key: <your-api-key>"
```

## Status

This project is under active development. Core functionality — schema, event ingestion, the rules engine, and API-key authentication — is complete and tested.
