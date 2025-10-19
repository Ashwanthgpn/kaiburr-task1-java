Kaiburr Task 1 — Spring Boot + MongoDB

Author: Ashwanth GPN

This repository implements Task-1 from Kaiburr Assessment 2025:

Java 17 + Spring Boot REST API

MongoDB persistence for Task documents

Endpoints to create/update, list, get by id, search by name, delete

Safe command execution with TaskExecution history (startTime, endTime, output)

Proof via HTTP client (PowerShell Invoke-RestMethod / curl.exe) with screenshots that include name & current time.

Prerequisites

Java 17 (Temurin/Adoptium or Oracle)

Maven 3.9+

MongoDB Community running locally (Windows service name: MongoDB)

PowerShell or VS Code Terminal

Verification:

java -version
mvn -v
Get-Service MongoDB


Screenshots:
Each screenshot includes a tiny terminal showing:
Write-Output "Ashwanth GPN — $(Get-Date)"

Build & Run
mvn clean package -DskipTests
mvn --% -Dspring-boot.run.mainClass=com.ashwanth.kaiburr.KaiburrApplication spring-boot:run
# (Optionally) java -jar target/kaiburr-task1-java-0.0.1-SNAPSHOT.jar


App start log (Tomcat 8080, Spring Boot started):


Data Model
{
  "id": "string",
  "name": "string",
  "owner": "string",
  "command": "string",
  "taskExecutions": [
    { "startTime": "date", "endTime": "date", "output": "string" }
  ]
}

API (base: http://localhost:8080/api)
1) Health

GET /health → { "status": "ok", "time": "..." }


2) Create/Update Task

PUT /tasks
Body (JSON):

{"name":"first-task","owner":"Ashwanth","command":"echo Hi"}


Response: Task object with id, name, owner, command, taskExecutions (empty initially).


3) List / Get by ID / Search by Name

GET /tasks — returns all tasks


GET /tasks?id=<TASK_ID> — returns a single task (404 if not found)


GET /tasks?name=<query> — case-insensitive contains search (404 if none)


4) Run Command & Append Execution

PUT /tasks/{id}/executions
Optional body (override the stored command for this run only):

{"command":"echo Hello from exec"}


Validates command safety (allow-list).

Appends a TaskExecution { startTime, endTime, output } to the task.


Verify execution history:

GET /tasks?id=<TASK_ID> now shows taskExecutions array with the last run:


5) Delete Task

DELETE /tasks/{id} — deletes a task by id.

Command Safety Policy

To prevent unsafe commands, the API only allows a small allow-list (e.g., echo, java -version, mvn -v).
Other commands return HTTP 400 with an error message.

Quick Test Snippets (PowerShell)
# Health
Invoke-RestMethod -Uri "http://localhost:8080/api/health"

# Create/Update
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/tasks" `
  -ContentType "application/json" `
  -Body '{"name":"first-task","owner":"Ashwanth","command":"echo Hi"}'

# List
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks"

# By ID (replace <ID>)
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks?id=<ID>" | ConvertTo-Json -Depth 6

# Search by name
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks?name=first"

# Run execution (replace <ID>)
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/tasks/<ID>/executions" `
  -ContentType "application/json" `
  -Body '{"command":"echo Hello from exec"}'

# Delete (replace <ID>)
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/tasks/<ID>"

Screenshots (Proof)

All screenshots include identity/time (Ashwanth GPN — $(Get-Date)) as required:

01-java-version.png — java -version

02-mvn-v.png — mvn -v

03-mongo-service.png — Get-Service MongoDB

04-app-start.png — Spring Boot startup with Tomcat 8080

05-health.png — GET /api/health

06-put-task.png — PUT /api/tasks

07-get-tasks.png — GET /api/tasks

08-get-task-by-id.png — GET /api/tasks?id=…

09-search-by-name.png — GET /api/tasks?name=…

10-put-execution.png — PUT /api/tasks/{id}/executions response

11-task-with-executions.png — final task with taskExecutions

Files are located at: docs/screens/*.png

How to Clean Previous Tasks (optional)

Delete by id:

Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/tasks/<ID>"


Delete all:

$ids = (Invoke-RestMethod -Uri "http://localhost:8080/api/tasks").id
$ids | ForEach-Object {
  Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/tasks/$_"
}

Project Structure
kaiburr-task1-java/
├─ pom.xml
├─ src/
│  └─ main/java/com/ashwanth/kaiburr/...
│     ├─ KaiburrApplication.java
│     ├─ api/TaskController.java
│     ├─ domain/Task.java
│     ├─ domain/TaskExecution.java
│     ├─ repo/TaskRepository.java
│     ├─ security/CommandPolicy.java
│     └─ service/CommandRunner.java
├─ src/main/resources/application.yml
├─ docs/screens/   (screenshots)
└─ README.md

(Optional) Swagger UI

Add to pom.xml:

<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.6.0</version>
</dependency>


Then open: http://localhost:8080/swagger-ui.html.

Notes

Java 17 + Spring Boot 3.x

MongoDB driver 5.x; localhost:27017

PowerShell used for API proof (Invoke-RestMethod)

All responses verified and persisted to MongoDB.

Author

Ashwanth GPN