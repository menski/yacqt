package org.menski;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.menski.dto.TokenRequest;
import org.menski.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TasklistClient implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(TasklistClient.class);

  private static final String ASSIGNEE = "yacqt";

  private static final TypeReference<GraphQlResponse<ClaimTaskResult>>
      CLAIM_TASK_RESULT_TYPE_REFERENCE = new TypeReference<>() {};
  private static final TypeReference<GraphQlResponse<CompleteTaskResult>>
      COMPLETE_TASK_RESULT_TYPE_REFERENCE = new TypeReference<>() {};
  private static final TypeReference<GraphQlResponse<GetTaskResult>>
      GET_TASK_RESULT_TYPE_REFERENCE = new TypeReference<>() {};
  private static final TypeReference<GraphQlResponse<QueryTasksResults>> QUERY_TASKS_RESULT_TYPE_REFERENCE = new TypeReference<>() {};

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String graphQlUrl;
  private final String bearerToken;

  public TasklistClient(ObjectMapper objectMapper) throws IOException {
    httpClient = new OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(10))
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(10))
        .writeTimeout(Duration.ofSeconds(10))
        .build();
    this.objectMapper = objectMapper;
    graphQlUrl = Configuration.getTasklistAddress() + "/graphql";
    bearerToken = requestToken();
  }

  public List<Long> queryTasks() throws IOException {
    GraphQlQuery query =
        new GraphQlQuery("query ($assignee: String!) { tasks( query: { state: CREATED, assignee: $assignee, candidateGroup: $assignee} ) { id } }", Map.of("assignee", ASSIGNEE));
    String response = executePostRequest(graphQlUrl, query);
    QueryTasksResults result =
        objectMapper.readValue(response, QUERY_TASKS_RESULT_TYPE_REFERENCE).data();
    return result != null ? result.tasks().stream().map(Task::id).map(Long::parseLong).collect(Collectors.toList()) : List.of();
  }

  public Task getTask(long taskId) throws IOException {
    GraphQlQuery query =
        new GraphQlQuery("query ($id: String!) { task(id: $id) { id } }", Map.of("id", taskId));
    String response = executePostRequest(graphQlUrl, query);
    GetTaskResult result =
        objectMapper.readValue(response, GET_TASK_RESULT_TYPE_REFERENCE).data();
    return result != null ? result.task() : null;
  }

  public boolean claimTask(long taskId) throws IOException {

    GraphQlQuery query =
        new GraphQlQuery(
            "mutation ($taskId: String!, $assignee: String) { claimTask( taskId: $taskId, assignee: $assignee ) { id, assignee }}",
            Map.of("taskId", taskId, "assignee", ASSIGNEE));

    String response = executePostRequest(graphQlUrl, query);

    ClaimTaskResult result =
        objectMapper.readValue(response, CLAIM_TASK_RESULT_TYPE_REFERENCE).data();

    return result != null && result.claimTask() != null && result.claimTask().assignee().equals(ASSIGNEE);
  }

  public boolean completeTask(long taskId) throws IOException {
    GraphQlQuery query =
        new GraphQlQuery(
            "mutation ($taskId: String!) { completeTask( taskId: $taskId, variables: [] ) { id, taskState }}",
            Map.of("taskId", taskId));

    String response = executePostRequest(graphQlUrl, query);

    CompleteTaskResult result =
        objectMapper.readValue(response, COMPLETE_TASK_RESULT_TYPE_REFERENCE).data();

    return result != null && result.completeTask() != null && result.completeTask().taskState().equals("COMPLETED");
  }

  private String requestToken() throws IOException {
    String response =
        executePostRequest(
            Configuration.getZeebeAuthorizationServerUrl(),
            TokenRequest.createTasklistTokenRequest());
    TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
    return tokenResponse.getAccessToken();
  }

  private String executePostRequest(String url, Object body) throws IOException {
    String requestJson = objectMapper.writeValueAsString(body);
    RequestBody requestBody = RequestBody.create(requestJson, MediaType.parse("application/json"));

    Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

    if (bearerToken != null) {
      requestBuilder.addHeader("Authorization", "Bearer " + bearerToken);
    }

    Request request = requestBuilder.build();

    Response response = httpClient.newCall(request).execute();
    ResponseBody responseBody = response.body();

    if (response.code() != 200 || responseBody == null) {
      LOG.error(
          "Failed to execute post request to URL {} with body {}, received code {} and body: {}",
          url,
          requestJson,
          response.code(),
          responseBody != null ? responseBody.string() : "");
      throw new IOException(
          String.format("Failed to execute post request (code: %d)", response.code()));
    }

    return responseBody.string();
  }

  @Override
  public void close() {}

  // DTOs
  record GraphQlResponse<T>(T data, List<GraphQlError> errors) {}
  record GraphQlQuery(String query, Map<String, Object> variables) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record GraphQlError(String message) {}

  record Task(String id, String assignee, String taskState) {}

  record ClaimTaskResult(Task claimTask) {}
  record CompleteTaskResult(Task completeTask) {}
  record GetTaskResult(Task task) {}
  record QueryTasksResults(List<Task> tasks) {}

}
