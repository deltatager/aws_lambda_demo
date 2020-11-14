package com.slongpre.lambda_demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
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
}
