package com.slongpre.lambda_demo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

public class JiraCloudConnector {

    private final JiraConfig jiraConfig;
    private final HttpClient client;
    private final HttpRequest issuesRequest;
    private final Map<Integer, JSONObject> issues;

    public JiraCloudConnector(JiraConfig jiraConfig) {
        this.jiraConfig = jiraConfig;
        issues = new HashMap<>();

        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var params = ("?jql=project = \"" + jiraConfig.getProject() +
                (jiraConfig.getSprintName() != null ? "\" AND Sprint = \"" + jiraConfig.getSprintName() : "") +
                "\" AND timespent != 0" + "&maxResults=500&fields=id,parent,summary")
                .replace(" ", "%20")
                .replace("\"", "%22");

        issuesRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(jiraConfig.getUrl() + "/rest/api/3/search" + params))
                .header("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString((jiraConfig.getEmail() + ":" + jiraConfig.getToken()).getBytes()))
                .build();

    }


    public List<LogWorkEntry> getAllWorklogs() {
        var logs = new LinkedList<LogWorkEntry>();

        client.sendAsync(issuesRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResults)
                .join()
                .forEach(i -> {
                    var issue = (JSONObject) i;
                    issues.put(issue.getInt("id"), issue);
                });

        issues.keySet().forEach(issueId -> {
            final var req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(jiraConfig.getUrl() + "/rest/api/3/issue/" + issueId + "/worklog"))
                    .header("Authorization", "Basic "
                            + Base64.getEncoder().encodeToString((jiraConfig.getEmail() + ":" + jiraConfig.getToken()).getBytes()))
                    .build();

            logs.addAll(client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::parseToEntity)
                    .join());
        });

        return logs;
    }

    private JSONArray parseJsonResults(HttpResponse<String> rb) {
        try {
            var results = new JSONObject(rb.body());
            return results.getJSONArray("issues");
        }
        catch (Exception e) {
            System.err.println("---- Erreur de parsing de la réponse ----");
            System.err.println(e.getLocalizedMessage());
            System.err.println("Réponse de JIRA: ");
            System.err.println(rb.body());
            System.exit(1);
        }
        return null;
    }

    private List<LogWorkEntry> parseToEntity(HttpResponse<String> rb) {
        var list = new LinkedList<LogWorkEntry>();
        var array = new JSONObject(rb.body()).getJSONArray("worklogs");

        array.forEach(o -> {
            var log = (JSONObject) o;
            try {
                list.add(LogWorkEntry.builder()
                        .taskId(issues.get(log.getInt("issueId")).getString("key"))
                        .userTask(getSummary(issues.get(log.getInt("issueId"))))
                        .userName(log.getJSONObject("author").getString("displayName"))
                        .logWorkDescription(getComment(log))
                        .logWorkDate(log.getString("created"))
                        .logWorkSeconds(log.getInt("timeSpentSeconds"))
                        .logWorkDateTime(LocalDateTime.parse(log.getString("created").replaceFirst(
                                "\\.[0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]",
                                "")))
                        .build());
            }
            catch (Exception e) {
                System.err.println("Caught \"" + e.getMessage() + "\" on a worklog from " + issues.get(log.getInt("issueId")).getString("key") + " by " + log.getJSONObject(
                        "author").getString("displayName"));
            }
        });

        return list;
    }

    private String getComment(JSONObject log) {
        String value;
        try {
            value = log.getJSONObject("comment").getJSONArray("content").getJSONObject(0).getJSONArray("content").getJSONObject(0).getString("text");
        }
        catch (Exception e) {
            if (jiraConfig.isIncludeEmptyComments())
                value = "--- EMPTY COMMENT ---";
            else
                throw e;
        }
        return value;
    }

    private String getSummary(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        if (obj.getJSONObject("fields").has("parent"))
            sb.append(obj.getJSONObject("fields").getJSONObject("parent").getJSONObject("fields").getString("summary")).append(" ");

        sb.append(obj.getJSONObject("fields").getString("summary"));
        return sb.toString();
    }
}
