# mask

A tokenization service: turns any JSON payload into a signed JWT, and back again.

## API

### `POST /api/token/generate`

Request body: any JSON value (object, array, or scalar).

```
curl -X POST http://localhost:8082/api/token/generate \
  -H 'Content-Type: application/json' \
  -d '{"userId": 101, "name": "Ratnesh", "role": "ADMIN"}'
```

Response:

```json
{ "token": "eyJhbGciOiJIUzM4NCJ9...." }
```

### `POST /api/token/decode`

Request body: `{ "token": "<jwt>" }`.

```
curl -X POST http://localhost:8082/api/token/decode \
  -H 'Content-Type: application/json' \
  -d '{"token": "eyJhbGciOiJIUzM4NCJ9...."}'
```

Response: the exact JSON payload that was passed to `/generate`.

### Errors

| Status | Cause |
|---|---|
| 400 | Missing/empty request body, or a blank `token` |
| 401 | Token is malformed, has an invalid signature, or has expired |

## Configuration

| Env var | Required | Default | Purpose |
|---|---|---|---|
| `JWT_SECRET` | yes | none | HMAC signing key. Must be a random string at least 32 bytes long. The app refuses to start without it. |
| `JWT_EXPIRATION_MS` | no | `3600000` (1 hour) | Token lifetime in milliseconds. |

Generate a strong secret, e.g.:

```
openssl rand -base64 48
```

## Running

```
JWT_SECRET=$(openssl rand -base64 48) ./mvnw spring-boot:run
```

## Testing

```
./mvnw test
```
