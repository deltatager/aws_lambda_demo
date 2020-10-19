package com.slongpre.lambda_demo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JiraCloudConnector {

    private final JiraConfig jiraConfig;
    private final HttpClient client;
    private final HttpRequest request;

    public JiraCloudConnector(JiraConfig jiraConfig) {
        this.jiraConfig = jiraConfig;

        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var params = ("?jql=project = \"" + jiraConfig.getProject() +
                (jiraConfig.getSprintName() != null ? "\" AND Sprint = \"" + jiraConfig.getSprintName() : "") +
                "\" AND timespent != 0" + "&maxResults=500&fields=id,worklog,parent,summary")
                .replace(" ", "%20")
                .replace("\"", "%22");

        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(jiraConfig.getUrl() + "/rest/api/3/search" + params))
                .header("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString((jiraConfig.getEmail() + ":" + jiraConfig.getToken()).getBytes()))
                .build();

    }


    public CompletableFuture<List<LogWorkEntry>> getAllIssuesAsync() {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResults)
                .thenApply(this::parseToEntity);
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

    private List<LogWorkEntry> parseToEntity(JSONArray array) {
        var list = new LinkedList<LogWorkEntry>();

        array.forEach(o -> {
            var obj = (JSONObject) o;
            obj.getJSONObject("fields").getJSONObject("worklog").getJSONArray("worklogs").forEach(wl -> {
                var log = (JSONObject) wl;
                try {
                    list.add(LogWorkEntry.builder()
                            .taskId(obj.getString("key"))
                            .userTask(getSummary(obj))
                            .userName(log.getJSONObject("author").getString("displayName"))
                            .logWorkDescription(getComment(log))
                            .logWorkDate(log.getString("created"))
                            .logWorkSeconds(log.getInt("timeSpentSeconds"))
                            .logWorkDateTime(LocalDateTime.parse(log.getString("created").replaceFirst("\\.[0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]", "")))
                            .build());
                }
                catch (Exception e) {
                    System.err.println("Caught \"" + e.getMessage() + "\" on a worklog from " + obj.getString("key") + " by " + log.getJSONObject("author").getString("displayName"));
                }
            });
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
