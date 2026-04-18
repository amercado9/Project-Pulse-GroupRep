# Testing Strategy

## Scope

- Backend only (Spring Boot)
- Frontend testing is not in scope at this time

---

## Test Types

### Unit Tests

- Test individual classes in isolation
- Use JUnit 5 and Mockito
- Mock dependencies (repositories, other services)
- Focus on service layer business logic
- No database or Spring context needed

### Integration Tests

- Test layers working together with a running Spring context
- Use `@SpringBootTest` for full context tests
- Use `@WebMvcTest` for controller layer tests
- Use `@DataJpaTest` for repository layer tests
- Use Testcontainers (MySQL) for realistic database integration

---

## Test Database

- Use Testcontainers with MySQL for integration tests
- Spring profile: `test`
- Configure via `src/test/resources/application-test.yml`
- Flyway disabled in test profile; JPA auto-generates schema (`ddl-auto: create-drop`)
- Testcontainers JDBC URL: `jdbc:tc:mysql:8.0:///testdb`

---

## What to Test

| Layer      | Test Type   | What to Verify                                     |
|------------|-------------|----------------------------------------------------|
| Service    | Unit        | Business logic, validation, edge cases, exceptions |
| Repository | Integration | Custom queries, data persistence                   |
| Controller | Integration | Request mapping, response format, status codes     |

---

## Naming Convention

- Test class: `{ClassName}Test` for unit tests, `{ClassName}IntegrationTest` for integration tests
- Test method: `should_ExpectedResult_When_Condition`
- Example: `should_ThrowNotFoundException_When_UserIdNotFound`

---

## Coverage Guidelines

- Service layer: aim for high coverage (business logic is the priority)
- Controller layer: cover happy path and key error scenarios
- Repository layer: cover custom query methods only (skip standard CRUD)

---

## Running Tests

```bash
./mvnw test
```
