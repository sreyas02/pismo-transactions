# Pismo Transactions API — Java / Spring Boot

A RESTful financial transactions service built with **Java 17 + Spring Boot 3**, clean layered architecture, full test coverage across three layers, and Docker support.

## Architecture

```
src/main/java/com/pismo/transactions/
  TransactionsApplication.java   → Spring Boot entry point
  domain/                        → JPA entities (Account, Transaction, OperationType)
  dto/                           → Request/Response DTOs (no domain leakage to HTTP)
  exception/                     → ValidationException, ResourceNotFoundException
  repository/                    → Spring Data JPA interfaces (DIP)
  validator/                     → Input validation component (SRP)
  service/                       → Business logic orchestration (SRP, DIP)
  controller/                    → HTTP handlers — thin layer (SRP)
  config/                        → GlobalExceptionHandler (centralised error mapping)
```

**Design principles applied:**
- **SRP** — domain, persistence, validation, orchestration, and HTTP are all separate layers
- **DIP** — services depend on repository *interfaces*; Spring injects concrete implementations
- **OCP** — sign rule lives in `OperationType.isDebit()` — adding a new type = one constant, nothing else
- **BigDecimal** throughout — never `double`/`float` for financial amounts

## Requirements

| Tool | Version |
|------|---------|
| Java | 17+     |
| Maven | 3.8+   |
| Docker + Docker Compose | optional |

## Quick Start

```bash
# Clone the repository
git clone https://github.com/sreyas02/pismo-transactions.git
cd pismo-transactions

# Option 1: Docker (zero setup — multi-arch ready for Apple Silicon M1/M2/M3 & Intel/AMD)
./run docker

# Option 2: Local (Java 17 + Maven required)
./run local

# Option 3: Tests (requires Java 17)
./run test

```

## Manual Commands

```bash
# Run locally
mvn spring-boot:run

# Run tests
mvn test

# Build fat JAR
mvn package -DskipTests

# Docker
docker compose up --build
```

## API Reference

### POST /accounts

**Request:**
```json
{ "document_number": "12345678900" }
```
**Response — 201:**
```json
{ "account_id": 1, "document_number": "12345678900" }
```
**Errors:** `422` — missing/empty/duplicate document_number

---

### GET /accounts/:accountId

**Response — 200:**
```json
{ "account_id": 1, "document_number": "12345678900" }
```
**Errors:** `400` — non-integer ID · `404` — not found

---

### POST /transactions

> **Sign rule enforced automatically:** debit operations (1, 2, 3) → negative amount; credit (4) → positive. Sign is corrected regardless of what the caller sends.

**Request:**
```json
{ "account_id": 1, "operation_type_id": 4, "amount": 123.45 }
```

| op type | description | stored sign |
|---------|-------------|-------------|
| 1 | Normal Purchase | negative |
| 2 | Purchase with installments | negative |
| 3 | Withdrawal | negative |
| 4 | Credit Voucher | positive |

**Response — 201:**
```json
{
  "transaction_id": 1,
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 123.45,
  "event_date": "2024-01-05T09:34:18"
}
```
**Errors:** `422` — any invalid field or missing account

---

## Example Session

```bash
# Create account
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"document_number": "12345678900"}' | jq

# Retrieve account
curl -s http://localhost:8080/accounts/1 | jq

# Purchase (stored as -50.0)
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id": 1, "operation_type_id": 1, "amount": 50.0}' | jq

# Credit voucher (stored as +60.0)
curl -s -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"account_id": 1, "operation_type_id": 4, "amount": 60.0}' | jq
```

## Test Coverage

Three test layers, same contract as the Go version:

| Layer | Class | What it proves |
|-------|-------|---------------|
| Domain unit | `OperationTypeTest` | Sign rule, isDebit() — zero Spring |
| Validator unit | `TransactionValidatorTest` | All input edge cases — zero Spring |
| Service unit | `TransactionServiceTest` | Business logic with Mockito mocks (DIP) |
| Controller slice | `AccountControllerTest` | HTTP contract — MockMvc, mocked service |
| Controller slice | `TransactionControllerTest` | HTTP contract — MockMvc, mocked service |
| E2E integration | `IntegrationTest` | Full flow with real H2 DB — @SpringBootTest |

```bash
./run test
```

## H2 Console

When running locally, H2's web console is available at:
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:pismoDb
Username: sa
Password: (empty)
```
