# Fraud Scoring Engine

A standalone B2B trust and fraud scoring service. Marketplace platforms (think OLX or Facebook Marketplace) send in behavioral events about their users — account creation, listings posted, complaints filed — and this service computes an explainable risk score on demand, backed by a configurable, data-driven rules engine.

This is infrastructure, not an application. Other backend systems call it as a service. A small demo page is included so the API can be tried in a browser.

**Live demo:** http://fraud-scoring-engine.duckdns.org:8080/

Register a client, submit an event and fetch a score, all from the page. API credentials are handled automatically behind the scenes.

## How it works

A marketplace platform registers as a client and receives an API key. The platform sends behavioral events for its users as they happen (`POST /events`). When the platform wants a risk assessment, it calls `GET /score/{userId}`. The service pulls that user's event history, runs it through a set of weighted rules, and returns a numeric score along with exactly which rules fired and why.

Every computed score is saved as a permanent, timestamped record — an audit trail, not just a live number.

## Core design decisions

### Multi-tenant by design

All clients' events live in a single shared table, but every row is scoped by `client_id`. A user ID is only meaningful in combination with the client it belongs to — two different platforms can each have a `user_123`, and they are treated as two entirely different people. Every query respects this.

### No PII, ever

The service only ever sees a pseudonymous user ID, an event type, a timestamp, and optional metadata supplied by the client. It was designed from the start to never need or store personal information.

### Rules as data, not code

Scoring logic is not a chain of hardcoded `if` statements. Each rule is an object with a name, a weight, and a condition function, and all rules live together in one place. Adding, removing, or reweighting a rule is a one-line change, not a refactor.

### Explainability over a black-box number

The API never returns a bare score. It always returns the score alongside the specific rules that triggered and their individual weights, so a caller (or a human reviewing a decision) can see exactly why a user was flagged.

### B2B API-key auth, not user login

Clients of this service are other backend platforms, not people. There is no login form or session — a client authenticates every request with an API key, verified against a bcrypt hash. Nothing is stored or returned in plaintext after the initial registration.

### Cached scores

Score lookups use a cache-aside pattern with Redis. A cached score is evicted whenever a new event arrives for that user, so callers never receive a stale result.

## Tech stack

* **Java 17 / Spring Boot** — REST API, dependency injection
* **PostgreSQL** — primary datastore, with JSONB for flexible event metadata
* **Spring Data JPA / Hibernate** — ORM layer
* **Spring Security** — API key authentication filter chain
* **BCrypt** — one-way hashing for API keys
* **Redis** — cache-aside caching for score lookups, with eviction on new events
* **Docker / Docker Compose** — full stack (app, Postgres, Redis) in containers
* **AWS EC2** — deployment
* **AWS SQS** *(planned)* — asynchronous event ingestion

## API overview

| Method | Endpoint            | Auth required     | Description                                                 |
| ------ | ------------------- | ----------------- | ----------------------------------------------------------- |
| `POST` | `/clients/register` | No                | Registers a new client platform, returns a one-time API key |
| `POST` | `/events`           | Yes (`X-API-Key`) | Records a behavioral event for a user                       |
| `GET`  | `/score/{userId}`   | Yes (`X-API-Key`) | Computes and returns the user's current risk score          |

Requests to protected endpoints without a valid `X-API-Key` header receive `401 Unauthorized`.

## Running locally

**Prerequisites:** Docker Desktop.

Start the whole stack (app, PostgreSQL and Redis):

```bash
docker compose up --build -d
```

The API and demo page are now available at `http://localhost:8080`.

To stop everything:

```bash
docker compose down
```

**Running from an IDE instead:** start only the databases with:

```bash
docker compose up -d postgres redis
```

Then run the Spring Boot app from your IDE (Java 17+ required). Point the datasource and Redis host at `localhost`.

## Example usage

### Register a client

```bash
curl -X POST http://localhost:8080/clients/register \
  -H "Content-Type: application/json" \
  -d '{"name": "Example Marketplace"}'
```

The response contains your `clientId` and `apiKey`. Save the key — it is shown only once.

### Send an event

```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: <your-api-key>" \
  -d '{"clientId": "<your-client-id>", "userId": "user_123", "type": "complaint_filed", "metadata": {"reason": "testing"}}'
```

### Get a score

```bash
curl http://localhost:8080/score/user_123 \
  -H "X-API-Key: <your-api-key>"
```

## Deployment

The project runs on an AWS EC2 instance using Docker Compose.

To deploy an update:

```bash
git pull
docker compose up --build -d
```

## Status

Core functionality is complete and deployed: schema, event ingestion, the rules engine, API-key authentication, Redis score caching and the Dockerised deployment.

Asynchronous ingestion through AWS SQS is planned next.
