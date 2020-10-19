package com.slongpre.lambda_demo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JiraReporter {

    private final JiraConfig jiraConfig;
    private final Map<String, Integer> secondsPerUser;

    public JiraReporter(JiraConfig jiraConfig) {
        this.jiraConfig = jiraConfig;
        this.secondsPerUser = new HashMap<>();
    }

    public String printReport(List<LogWorkEntry> entries) {
        LocalDate from = parseDate(jiraConfig.getStartDate());
        final Map<String, List<LogWorkEntry>> result =
                entries.stream()
                        .filter(wl -> wl.getLogWorkDateTime().toLocalDate().isAfter(from))
                        .collect(Collectors.groupingBy(LogWorkEntry::getUserName));

        StringBuilder sb = new StringBuilder();
        result.entrySet().forEach(e -> sb.append(printEntry(e)));

        return sb.toString();
    }

    private LocalDate parseDate(String dateFrom) {
        System.out.println("Date From: " + dateFrom);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
        return LocalDate.parse(dateFrom, dtf);
    }

    private String printEntry(Map.Entry<String, List<LogWorkEntry>> e) {
        secondsPerUser.put(e.getKey(), 0);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append(e.getKey()).append("<br/>");
        e.getValue().forEach(log -> sb.append(printEntryDetail(e.getKey(), log)));

        sb.append("Total time : ")
                .append(prettyPrintTime(secondsPerUser.get(e.getKey())))
                .append("<br/><br/>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String printEntryDetail(String user, LogWorkEntry log) {
        final int seconds = log.getLogWorkSeconds();
        StringBuilder sb = new StringBuilder();
        sb.append("    ")
                .append(log.getTaskId()).append(" ")
                .append(log.getLogWorkDescription()).append(" ")
                .append(log.getLogWorkDate()).append(" ")
                .append(log.getUserTask()).append(" ")
                .append(prettyPrintTime(seconds))
                .append("<br/>");
        int newSeconds = secondsPerUser.get(user) + seconds;
        secondsPerUser.put(user, newSeconds);

        return sb.toString();
    }

    private String prettyPrintTime(int seconds) {
        return Duration.ofSeconds(seconds)
                .toString()
                .substring(2)
                .toLowerCase()
                .replaceAll(".[wdhm]", "$0 ");
    }
}
