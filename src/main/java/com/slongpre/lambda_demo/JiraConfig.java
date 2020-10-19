package com.slongpre.lambda_demo;

import lombok.Data;

@Data
public class JiraConfig {

    private String email;
    private String token;
    private String url;
    private String project;
    private String sprintName;
    private String startDate;
    private boolean includeEmptyComments;
}
