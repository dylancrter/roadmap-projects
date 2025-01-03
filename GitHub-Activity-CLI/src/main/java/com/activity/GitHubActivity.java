package com.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GitHubActivity {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong usage.");
            return;
        }
        String username = args[0];
        int n = Integer.parseInt(args[1]);
        makeRequest(username, n);
    }

    public static void makeRequest(String username, int n) {
        try {
            String url = String.format("https://api.github.com/users/%s/events", username);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                printLatestActions(response.body(), n);
            } else {
                System.out.println(response.statusCode() + " error. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printLatestActions(String jsonString, int n) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);

        List<String> actions = new ArrayList<>();

        for (JsonNode event : rootNode) {
            String action = null;

            String type = event.path("type").asText("Unknown");

            JsonNode repoNode = event.path("repo");
            String repoName = repoNode.path("name").asText("Unknown");

            JsonNode payload = event.path("payload");
            String refType = payload.path("ref_type").asText("");
            String payloadAction = payload.path("action").asText("");
            JsonNode pullRequest = payload.path("pull_request");
            boolean merged = pullRequest.path("merged").asBoolean(false);

            switch (type) {
                case "PushEvent":
                    int size = payload.path("size").asInt(0);
                    action = String.format("Pushed %d commit(s) to %s", size, repoName);
                    break;
                case "IssueEvent":
                    action = String.format("Opened a new issue in %s", repoName);
                    break;
                case "WatchEvent":
                    action = String.format("Starred %s", repoName);
                    break;
                case "CreateEvent":
                    if ("repository".equals(refType)) {
                        action = String.format("Created a new repository %s", repoName);
                    }
                    break;
                case "ForkEvent":
                    JsonNode forkee = payload.path("forkee");
                    String name = forkee.path("name").asText("Unknown");
                    action = String.format("Forked %s to %s", repoName, name);
                    break;
                case "PullRequestEvent":
                    if ("opened".equals(payloadAction)) {
                        action = String.format("Opened a pull request in %s", repoName);
                    } else if ("closed".equals(payloadAction) && merged) {
                        action = String.format("Merged a pull request in %s", repoName);
                    }
                    break;
            }

            if (action != null) {
                actions.add(action);
                if (actions.size() == n) {
                    break;
                }
            }
        }

        System.out.println("Latest " + actions.size() + " actions:");
        for (String action : actions) {
            System.out.println(action);
        }
    }
}
