package com.ashwanth.kaiburr.domain;

import java.util.Date;

public class TaskExecution {
    private Date startTime;
    private Date endTime;
    private String output;

    public TaskExecution() {}

    public TaskExecution(Date startTime) {
        this.startTime = startTime;
    }

    // getters/setters
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public String getOutput() { return output; }

    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public void setOutput(String output) { this.output = output; }
}
