package com.ashwanth.kaiburr.api;

import com.ashwanth.kaiburr.domain.Task;
import com.ashwanth.kaiburr.domain.TaskExecution;
import com.ashwanth.kaiburr.repo.TaskRepository;
import com.ashwanth.kaiburr.security.CommandPolicy;
import com.ashwanth.kaiburr.service.CommandRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TaskController {
    private final TaskRepository tasks;
    private final CommandRunner runner = new CommandRunner();

    public TaskController(TaskRepository tasks) {
        this.tasks = tasks;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "time", new Date().toInstant().toString());
    }

    /** GET tasks (all). If id query param present → return that task or 404; if name present → contains search */
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(@RequestParam(value = "id", required = false) String id,
                                      @RequestParam(value = "name", required = false) String nameContains) {
        if (id != null && !id.isBlank()) {
            return tasks.findById(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Task not found")));
        }
        if (nameContains != null && !nameContains.isBlank()) {
            var found = tasks.findByNameContainingIgnoreCase(nameContains);
            if (found.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No tasks match name query"));
            }
            return ResponseEntity.ok(found);
        }
        return ResponseEntity.ok(tasks.findAll());
    }

    /** PUT a task (create or update). Validates command safety. */
    @PutMapping("/tasks")
    public ResponseEntity<?> putTask(@RequestBody Task body) {
        var cmd = body.getCommand();
        if (cmd != null && !cmd.isBlank() && !CommandPolicy.allowed(cmd)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Command not allowed by policy",
                    "allowedExamples", List.of("echo Hello", "java -version", "mvn -v")
            ));
        }
        Task saved = tasks.save(body); // upsert (create or update if id present)
        return ResponseEntity.ok(saved);
    }

    /** DELETE a task by id */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id) {
        if (!tasks.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        }
        tasks.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** PUT a TaskExecution (by task ID): run the command and append execution */
    @PutMapping("/tasks/{id}/executions")
    public ResponseEntity<?> runAndAppend(@PathVariable String id,
                                          @RequestBody(required = false) Map<String, String> body) throws Exception {
        var taskOpt = tasks.findById(id);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        }
        Task task = taskOpt.get();

        // ensure the list is non-null (older docs can have null)
        if (task.getTaskExecutions() == null) {
            task.setTaskExecutions(new ArrayList<>());
        }

        // command from body or fallback to task.command
        String cmd = (body != null ? body.get("command") : null);
        if (cmd == null || cmd.isBlank()) cmd = task.getCommand();
        if (cmd == null || cmd.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No command provided on task or request"));
        }
        if (!CommandPolicy.allowed(cmd)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Command not allowed by policy"));
        }

        TaskExecution ex = new TaskExecution(new Date());
        var res = runner.run(cmd, java.time.Duration.ofSeconds(15));
        ex.setEndTime(new Date());
        // combine stdout+stderr as output
        ex.setOutput((res.stdout == null ? "" : res.stdout) + (res.stderr == null ? "" : res.stderr));

        task.getTaskExecutions().add(ex);
        tasks.save(task);

        return ResponseEntity.ok(Map.of(
                "id", task.getId(),
                "name", task.getName(),
                "owner", task.getOwner(),
                "command", cmd,
                "lastExecution", ex
        ));
    }
}
