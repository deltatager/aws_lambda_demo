package com.slongpre.lambda_demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class JiraHoursHandler implements RequestHandler<JiraConfig, String> {

    @Override
    public String handleRequest(JiraConfig jiraConfig, Context context) {
        return new JiraReporter(jiraConfig)
                .printReport(new JiraCloudConnector(jiraConfig).getAllWorklogs());
    }
}
