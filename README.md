# AnyMind BTC V1

## Specs
- **Functional**: 
  - Record BTC incomes (`POST /api/v1/records`) with event time in the **past** (future beyond small skew is rejected).
  - Query **UTC end-of-hour** balances between two datetimes (`POST /api/v1/balances/history`).
- **Non-functional**:
  - **Scalable reads** via CQRS-lite: write events to `txn`, batch-project **hourly_delta** (one row per hour).
  - **Eventual consistency** is acceptable; projector flushes ~1s by default.
  - Run anywhere: Dockerfile + docker-compose (db + app).

## Tech stack
- **Kotlin 2.1.21**, **Spring Boot 3.3**, **Java 21**
- JPA (Hibernate), Flyway, PostgreSQL
- Time: store and emit **UTC** (`TIMESTAMPTZ`, `Instant`)

## Run (one command)
```bash
docker compose up --build --force-recreate app
```

## API
### Save Record
`POST /api/v1/records`
```json
{ "datetime": "2019-10-05T14:45:05+07:00", "amount": 1.1 }
```
Response `201`:
```json
{ "id": "<uuid>", "status": "ok" }
```

### Balance History (end-of-hour snapshots)
`GET /api/v1/balances/history?startDatetime=2019-10-05T12:48:01%2B00:00&endDatetime=2019-10-05T17:48:02%2B00:00`

Returns ordered list of `{ datetime: "HH:00:00+00:00", amount }`.

## Notes
- Projector is in-process with advisory lock (single leader). If the app is down, `txn` keeps events; on restart the projector **catches up** from the watermark.
- Future guard: `datetime` must not be more than `app.future-skew-seconds` ahead (default 120s).
