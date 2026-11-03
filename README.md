# Employee RESTful API

Completed Spring Boot assignment using the existing Controller -> Service -> Repository structure, Lombok, MapStruct, and Jakarta Validation.

## Requirements and running

- JDK 25 (select JDK 25 as the Project SDK and Gradle JVM in IntelliJ).
- The Gradle wrapper is included; a separate Gradle installation is unnecessary.
- Internet access is needed for the first Gradle/dependency download.
- No database setup is required. The original in-memory repository is retained.

Open a terminal in the folder that contains `build.gradle`:

```bash
chmod +x gradlew
./gradlew clean build
./gradlew bootRun
```

On Windows, use `gradlew.bat clean build` and `gradlew.bat bootRun`.

- Base URL: http://localhost:8888/api/v1/employees
- Swagger UI: http://localhost:8888/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8888/v3/api-docs
- Press **Ctrl+C** to stop the application.

## Endpoints

| Method | Path | Success response |
| --- | --- | --- |
| GET | `/api/v1/employees` | 200, list of employees |
| GET | `/api/v1/employees/{id}` | 200, one employee |
| POST | `/api/v1/employees` | 201, created employee and `Location` header |
| PUT | `/api/v1/employees/{id}` | 200, updated employee |
| DELETE | `/api/v1/employees/{id}` | 204, empty response body |
| GET | `/api/v1/employees/search?name=value` | 200, matching employees |

`PUT` replaces all editable fields. Include `name`, `email`, `salary`, `hireDate`, and `isActive` in the request. The employee ID is taken from the URL and stays unchanged. Delete permanently removes the employee from the in-memory list.

The application starts with **Tony Stark (ID 1)** and **Bruce Banner (ID 2)**. New employees receive generated IDs and `isActive: true`. Data changes are lost when the application restarts.

## Try the API

### 1. List or get one employee

```bash
curl http://localhost:8888/api/v1/employees
curl http://localhost:8888/api/v1/employees/1
```

### 2. Create

```bash
curl -i -X POST http://localhost:8888/api/v1/employees \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Yin Wattana",
    "email": "wattana@example.com",
    "salary": 1200.00,
    "hireDate": "2026-11-03"
  }'
```

Use the returned ID for update and delete. Immediately after a fresh restart, the first new employee has ID 3.

### 3. Update

```bash
curl -i -X PUT http://localhost:8888/api/v1/employees/3 \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Yin Wattana Updated",
    "email": "wattana@example.com",
    "salary": 1500.00,
    "hireDate": "2026-11-03",
    "isActive": false
  }'
```

An employee may keep their own email or change it to an unused address. Using another employee's email returns **409 Conflict**. Email uniqueness ignores case and outer whitespace.

### 4. Search by name

```bash
curl --get http://localhost:8888/api/v1/employees/search \
  --data-urlencode 'name=tony'
```

Search matches any part of the name, ignores letter case, and trims outer whitespace. For example, `tony`, `TONY`, and `ony Sta` match Tony Stark. It includes both active and inactive employees. No matches return `[]`. Missing, blank, or longer-than-100-character search terms return **400 Bad Request**.

### 5. Delete

```bash
curl -i -X DELETE http://localhost:8888/api/v1/employees/3
```

Reading, updating, or deleting an unknown ID returns **404 Not Found**.

## Request validation

| Field | Create | Update | Rules |
| --- | --- | --- | --- |
| `name` | Required | Required | Nonblank, 2-100 characters after trimming |
| `email` | Required | Required | Nonblank, valid email, at most 254 characters, unique |
| `salary` | Required | Required | Number greater than zero |
| `hireDate` | Required | Required | Valid ISO date, for example `2026-11-03` |
| `isActive` | Automatically true | Required | Boolean `true` or `false`; cannot be null |

Both request records trim names and email addresses before validation. `@Valid` in the controller triggers DTO validation. There is no restriction against future hire dates. Malformed JSON, invalid dates/types, and a nonnumeric ID return **400**.

Validation errors use the existing error-response format, for example:

```json
{
  "status": false,
  "code": 400,
  "message": "Validation failed",
  "errors": [
    { "field": "salary", "message": "Salary must be greater than 0" }
  ],
  "timestamp": "2026-11-03T18:00:00"
}
```

## Where the changes are

- `controller/EmployeeController.java`: CRUD routes, search, request validation, HTTP statuses.
- `dto/UpdateEmployeeRequest.java`: update fields and validation constraints.
- `dto/CreateEmployeeRequest.java`: consistent required-field validation and text trimming.
- `service/impl/EmployeeServiceImpl.java`: update, delete, partial-name search, unique-email checks, and missing-ID handling. Synchronized methods protect the shared in-memory list during concurrent requests.
- `mapper/EmployeeMapper.java`: MapStruct update mapping preserves IDs; unmapped target properties fail compilation.
- `exception/CustomExceptionHandling.java`: consistent JSON errors for validation, missing employees, duplicate emails, and malformed requests.
- `build.gradle`: Lombok/MapStruct annotation-processing binding.
- `src/test/java/co/istad/restfulemployeeapi/EmployeeApiTests.java`: API integration tests.

## Verification

Run the complete build and API test suite:

```bash
./gradlew clean build
```

Tests cover the CRUD lifecycle, create response location, all update fields, duplicate-email conflicts, unchanged data after rejected updates, missing IDs, search matching/no matches, missing and invalid query values, invalid dates, and malformed JSON. The HTML test report is generated at `build/reports/tests/test/index.html`.
