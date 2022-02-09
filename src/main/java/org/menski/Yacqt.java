package org.menski;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Topology;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

public class Yacqt {

  private static final Logger LOG = LoggerFactory.getLogger(Yacqt.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Set<Long> processInstanceKeys = new ConcurrentSkipListSet<>();

  private void run() {
    try (ZeebeClient zeebeClient = createZeebeClient(); TasklistClient tasklistClient = new TasklistClient(objectMapper); Workers workers = new Workers(zeebeClient)) {
      // verify we can connect to the cluster
      connectToCluster(zeebeClient);

      deployProcesses(zeebeClient);

      int instances = Configuration.getZeebeInstances();
      processInstanceKeys.add(startInstance(zeebeClient, "smoke-test", instances));
      processInstanceKeys.add(startInstance(zeebeClient, "task-test", instances));
      processInstanceKeys.add(startInstance(zeebeClient, "timer-test", instances));

      workers.initWorkers(processInstanceKeys);

      LOG.info("Waiting for process instances to finish");
      while (!processInstanceKeys.isEmpty()) {
        completeUserTasks(tasklistClient);
      }
      LOG.info("All process instances finished");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private long startInstance(ZeebeClient zeebeClient, String processName, int instances) {
    Map<String, Object> variableMap =
        Map.of(
            "processName",
            processName,
            "businessKeys",
            IntStream.range(0, instances).mapToObj(i -> UUID.randomUUID().toString()).toList());

    long processInstanceKey = zeebeClient.newCreateInstanceCommand()
        .bpmnProcessId("process-starter")
        .latestVersion()
        .variables(variableMap)
        .send()
        .join()
        .getProcessInstanceKey();

    LOG.info("Started process instance with key {} for process name {} with instances {}", processInstanceKey, processName, instances);

    return processInstanceKey;
  }

  private void deployProcesses(ZeebeClient zeebeClient) {
    LOG.debug("Deploying processes to cluster");
    DeploymentEvent deploymentEvent =
        zeebeClient
            .newDeployCommand()
            .addResourceFromClasspath("bpmn/process-starter.bpmn")
            .addResourceFromClasspath("bpmn/smoke-test.bpmn")
            .addResourceFromClasspath("bpmn/task-test.bpmn")
            .addResourceFromClasspath("bpmn/timer-test.bpmn")
            .send()
            .join();

    deploymentEvent
        .getProcesses()
        .forEach(
            process ->
                LOG.info(
                    "Deployed process {} from resource {} with key {} and version {}",
                    process.getBpmnProcessId(),
                    process.getResourceName(),
                    process.getProcessDefinitionKey(),
                    process.getVersion()));
  }

  private ZeebeClient createZeebeClient() {
    return ZeebeClient.newClientBuilder()
        .gatewayAddress(Configuration.getZeebeAddress())
        .numJobWorkerExecutionThreads(4)
        .credentialsProvider(
            new OAuthCredentialsProviderBuilder()
                .clientId(Configuration.getZeebeClientId())
                .clientSecret(Configuration.getZeebeClientSecret())
                .audience(Configuration.getZeebeAudience())
                .authorizationServerUrl(Configuration.getZeebeAuthorizationServerUrl())
                .build())
        .build();
  }

  private void completeUserTasks(TasklistClient tasklistClient) throws IOException {
    for (Long taskKey : tasklistClient.queryTasks()) {
      if (!tasklistClient.claimTask(taskKey)) {
        LOG.warn("Failed to assignee user task {}", taskKey);
      }

      if (!tasklistClient.completeTask(taskKey)) {
        LOG.error("Failed to complete user task {}", taskKey);
      }
    }
  }

  private void connectToCluster(ZeebeClient zeebeClient) throws JsonProcessingException {
    LOG.debug(
        "Trying to connect to cluster {} with client id {}",
        Configuration.getZeebeAddress(),
        Configuration.getZeebeClientId());
    Topology topology = zeebeClient.newTopologyRequest().send().join();
    String topologyString =
        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(topology);
    LOG.info("Connected to cluster {}", topologyString);
  }

  public static void main(String[] args) {
    new Yacqt().run();
  }
}
