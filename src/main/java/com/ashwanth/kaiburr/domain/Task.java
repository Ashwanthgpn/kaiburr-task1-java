package com.ashwanth.kaiburr.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private String name;
    private String owner;     // <— required by spec
    private String command;
    private List<TaskExecution> taskExecutions = new ArrayList<>();

    public Task() {}
    public Task(String name, String owner, String command) {
        this.name = name;
        this.owner = owner;
        this.command = command;
    }

    // getters/setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public String getCommand() { return command; }
    public List<TaskExecution> getTaskExecutions() { return taskExecutions; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOwner(String owner) { this.owner = owner; }
    public void setCommand(String command) { this.command = command; }
    public void setTaskExecutions(List<TaskExecution> taskExecutions) { this.taskExecutions = taskExecutions; }
}
