package org.menski;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class Workers implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(Workers.class);

  private final ZeebeClient zeebeClient;

  private JobWorker startProcessWorker;
  private JobWorker finishProcessWorker;
  private JobWorker noopWorker;
  private JobWorker publishMessageWorker;
  private JobWorker reportProcessFinishedWorker;

  public Workers(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  public void initWorkers(Set<Long> processInstanceKeys) {
    startProcessWorker =
        zeebeClient.newWorker().jobType("start-process").handler(this::startProcess).open();
    LOG.debug("Started worker for job type 'start-process'");
    finishProcessWorker =
        zeebeClient.newWorker().jobType("finish-process").handler(this::finishProcess).open();
    LOG.debug("Started worker for job type 'finish-process'");
    noopWorker = zeebeClient.newWorker().jobType("noop").handler(this::noop).open();
    LOG.debug("Started worker for job type 'noop'");
    publishMessageWorker =
        zeebeClient.newWorker().jobType("publish-message").handler(this::publishMessage).open();
    LOG.debug("Started worker for job type 'publish-message'");

    reportProcessFinishedWorker =
        zeebeClient
            .newWorker()
            .jobType("report-process-finished")
            .handler(
                (client, job) -> {
                  if (processInstanceKeys.contains(job.getProcessInstanceKey())) {
                    client.newCompleteCommand(job).send().join();
                    processInstanceKeys.remove(job.getProcessInstanceKey());
                  } else {
                    client
                        .newFailCommand(job)
                        .retries(0)
                        .errorMessage("Not waiting for process instance")
                        .send()
                        .join();
                  }
                })
            .open();
    LOG.debug("Started worker for type 'report-process-finished'");
  }

  public void startProcess(JobClient jobClient, ActivatedJob job) {
    ParentProcessVariables variables = job.getVariablesAsType(ParentProcessVariables.class);

    Map<String, Object> variablesMap = new HashMap<>();

    // pass business key as variable to use for return correlation
    variablesMap.put("businessKey", variables.businessKey());
    // pass process instance key as variable to use for progress update
    variablesMap.put("parentProcessInstanceKey", job.getProcessInstanceKey());

    LOG.debug(
        "Starting process with process name {}, business key {} and variables {}",
        variables.processName(),
        variables.businessKey(),
        variablesMap);

    publishMessageChecked(
        zeebeClient
            .newPublishMessageCommand()
            .messageName(variables.processName())
            .correlationKey(variables.businessKey())
            .messageId("start-process-" + variables.businessKey())
            .timeToLive(Duration.ofMinutes(10))
            .variables(variablesMap));

    jobClient.newCompleteCommand(job).send().join();
  }

  public void finishProcess(JobClient jobClient, ActivatedJob job) {
    ChildProcessVariables variables = job.getVariablesAsType(ChildProcessVariables.class);
    publishMessageChecked(
        zeebeClient
            .newPublishMessageCommand()
            .messageName("process-finished")
            .correlationKey(variables.businessKey())
            .messageId("process-finished-" + variables.businessKey())
            .timeToLive(Duration.ofMinutes(10))
            .variables(Map.of("processInstanceKey", job.getProcessInstanceKey())));
    jobClient.newCompleteCommand(job).send().join();
  }

  public void noop(JobClient jobClient, ActivatedJob job) {
    jobClient.newCompleteCommand(job).send().join();
  }

  public void publishMessage(JobClient jobClient, ActivatedJob job) {
    String messageName = Objects.requireNonNull(job.getCustomHeaders().get("messageName"));
    String correlationKey = UUID.randomUUID().toString();

    zeebeClient
        .newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .timeToLive(Duration.ofMinutes(5))
        .send()
        .join();

    jobClient
        .newCompleteCommand(job)
        .variables(Map.of("correlationKey", correlationKey))
        .send()
        .join();
  }

  private void publishMessageChecked(
      PublishMessageCommandStep1.PublishMessageCommandStep3 command) {
    try {
      command.send().join();
    } catch (Exception e) {
      if (!(e instanceof ClientStatusException)
          || ((ClientStatusException) e).getStatus() != Status.ALREADY_EXISTS) {
        throw e;
      }
    }
  }

  @Override
  public void close() {
    noopWorker.close();
    LOG.debug("Closed worker for type 'noop'");
    publishMessageWorker.close();
    LOG.debug("Closed worker for type 'publish-message'");
    finishProcessWorker.close();
    LOG.debug("Closed worker for type 'finish-process'");
    startProcessWorker.close();
    LOG.debug("Closed worker for type 'start-process'");
    reportProcessFinishedWorker.close();
    LOG.debug("Closing worker for type 'report-process-finished'");
  }

  record ParentProcessVariables(String processName, String businessKey) {}

  record ChildProcessVariables(String businessKey, Long parentProcessInstanceKey) {}
}
