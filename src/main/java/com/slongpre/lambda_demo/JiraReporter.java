package com.slongpre.lambda_demo;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class JiraReporter {

    private final JiraConfig jiraConfig;
    private final Map<String, Integer> secondsPerUser;

    public JiraReporter(JiraConfig jiraConfig) {
        this.jiraConfig = jiraConfig;
        this.secondsPerUser = new HashMap<>();
    }

    public String printReport(List<LogWorkEntry> entries) {
        LocalDateTime from = parseDate(jiraConfig.getStartDateTime());
        final Map<String, List<LogWorkEntry>> result =
                entries.stream()
                        .filter(wl -> wl.getLogWorkDateTime().isAfter(from))
                        .collect(Collectors.groupingBy(LogWorkEntry::getUserName));

        result.forEach((username, list) -> list.sort(Comparator.comparing(LogWorkEntry::getLogWorkDateTime)));

        StringBuilder sb = new StringBuilder();
        result.entrySet().forEach(e -> sb.append(printEntry(e)));

        return sb.toString();
    }

    private LocalDateTime parseDate(String dateFrom) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
        var date = LocalDateTime.parse(dateFrom, dtf);
        System.out.println("Date From: " + date);
        return date;
    }

    private String printEntry(Map.Entry<String, List<LogWorkEntry>> e) {
        secondsPerUser.put(e.getKey(), 0);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: monospace;'>");
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
        sb.append(" &emsp; ")
                .append(StringUtils.rightPad(log.getTaskId(), 10, '_') )
                .append(StringUtils.rightPad(prettyPrintTime(seconds), 8, '_'))
                .append(log.getLogWorkDate()).append(" ")
                .append(StringUtils.rightPad(log.getUserTask(), 120, '_'))
                .append(log.getLogWorkDescription())
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
