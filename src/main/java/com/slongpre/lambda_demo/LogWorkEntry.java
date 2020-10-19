package com.slongpre.lambda_demo;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Builder
@AllArgsConstructor
public class LogWorkEntry {
    private String taskId;
    private String userTask;
    private String userName;
    private String logWorkDescription;
    private String logWorkDate;
    private int logWorkSeconds;
    private LocalDateTime logWorkDateTime;

    public LogWorkEntry(String taskId, String userTask, String userName, String logWorkDescription, String logWorkDate, int logWorkSeconds) {
        this.taskId = taskId;
        this.userTask = userTask;
        this.userName = userName;
        this.logWorkDescription = logWorkDescription;
        this.logWorkDate = logWorkDate;
        this.logWorkSeconds = logWorkSeconds;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/LLL/yy h:mm a", Locale.US);
        this.logWorkDateTime = LocalDateTime.parse(logWorkDate, dtf);
    }
    @Override
    public String toString() {
        return "LogWorkEntry{" +
                "taskId='" + taskId + '\'' +
                ", userTask='" + userTask + '\'' +
                ", userName='" + userName + '\'' +
                ", logWorkDescription='" + logWorkDescription + '\'' +
                ", logWorkDate='" + logWorkDateTime + '\'' +
                ", logWorkSeconds=" + logWorkSeconds +
                '}';
    }


    public String getTaskId() {
        return taskId;
    }

    public String getUserTask() {
        return userTask;
    }

    public String getUserName() {
        return userName;
    }

    public String getLogWorkDescription() {
        return logWorkDescription;
    }

    public String getLogWorkDate() {
        return logWorkDate;
    }

    public int getLogWorkSeconds() {
        return logWorkSeconds;
    }

    public LocalDateTime getLogWorkDateTime() {
        return logWorkDateTime;
    }
}
