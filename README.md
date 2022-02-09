# yacqt
Yet Another Camunda Cloud Quality Assurance Tool

## How to use it

1. [Create a cluster](https://docs.camunda.io/docs/components/cloud-console/manage-clusters/create-cluster/) in Camunda Cloud
1. Create a set of [API credentials](https://docs.camunda.io/docs/components/cloud-console/manage-clusters/create-cluster/) in the cluster and make sure to select also the tasklist scope and download the credentials
1. Checkout this repository
    ```
    git clone https://github.com/menski/yacqt.git
    cd yacqt
    ```
1. Source your credentials and run the application
    ```
    source CamundaCloudMgmtAPI-Client-test-client.txt
    mvn complie exec:java
    ```
1. The output will look similar to this
    ```
    [INFO] Scanning for projects...
    [INFO]
    [INFO] --------------------------< org.menski:yacqt >--------------------------
    [INFO] Building yacqt 1.0-SNAPSHOT
    [INFO] --------------------------------[ jar ]---------------------------------
    [INFO]
    [INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ yacqt ---
    [INFO] Using 'UTF-8' encoding to copy filtered resources.
    [INFO] Copying 5 resources
    [INFO]
    [INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ yacqt ---
    [INFO] Changes detected - recompiling the module!
    [INFO] Compiling 6 source files to /home/menski/github/menski/yacqt/target/classes
    [INFO]
    [INFO] --- exec-maven-plugin:3.0.0:java (default-cli) @ yacqt ---
    22:36:09.191 [org.menski.Yacqt.main()] DEBUG org.menski.Yacqt - Trying to connect to cluster 01b5bbf4-a6e9-46e8-9d39-3246a57c5751.bru-2.zeebe.camunda.io:443 with client id HD_2YbONkqM~bhYQdGba3e8d-MP4Nc_~
    22:36:09.581 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Connected to cluster {
      "brokers" : [ {
        "nodeId" : 1,
        "host" : "zeebe-1.zeebe-broker-service.01b5bbf4-a6e9-46e8-9d39-3246a57c5751-zeebe.svc.cluster.local",
        "port" : 26501,
        "version" : "1.4.0-alpha1",
        "partitions" : [ {
          "partitionId" : 1,
          "role" : "LEADER",
          "health" : "HEALTHY",
          "leader" : true
        }, {
          "partitionId" : 2,
          "role" : "LEADER",
          "health" : "HEALTHY",
          "leader" : true
        } ],
        "address" : "zeebe-1.zeebe-broker-service.01b5bbf4-a6e9-46e8-9d39-3246a57c5751-zeebe.svc.cluster.local:26501"
      }, {
        "nodeId" : 0,
        "host" : "zeebe-0.zeebe-broker-service.01b5bbf4-a6e9-46e8-9d39-3246a57c5751-zeebe.svc.cluster.local",
        "port" : 26501,
        "version" : "1.4.0-alpha1",
        "partitions" : [ {
          "partitionId" : 1,
          "role" : "FOLLOWER",
          "health" : "HEALTHY",
          "leader" : false
        }, {
          "partitionId" : 2,
          "role" : "FOLLOWER",
          "health" : "HEALTHY",
          "leader" : false
        } ],
        "address" : "zeebe-0.zeebe-broker-service.01b5bbf4-a6e9-46e8-9d39-3246a57c5751-zeebe.svc.cluster.local:26501"
      }, {
        "nodeId" : 2,
        "host" : "zeebe-2.zeebe-broker-service.01b5bbf4-a6e9-46e8-9d39-3246a57c5751-zeebe.svc.cluster.local",
        "port" : 26501,
        "version" : "1.4.0-alpha1",
        "partitions" : [ {
          "partitionId" : 1,
          "role" : "FOLLOWER",
          "health" : "HEALTHY",
          "leader" : false
        }, {
          "partitionId" : 2,
          "role" : "FOLLOWER",
          "health" : "HEALTHY",
          "leader" : false
        } ],
        "address" : "zeebe-2.zeebe-broker-service.01b5bbf4-a6e9-46e8-9d39-3246a57c5751-zeebe.svc.cluster.local:26501"
      } ],
      "clusterSize" : 3,
      "partitionsCount" : 2,
      "replicationFactor" : 3,
      "gatewayVersion" : "1.4.0-alpha1"
    }
    22:36:09.582 [org.menski.Yacqt.main()] DEBUG org.menski.Yacqt - Deploying processes to cluster
    22:36:09.857 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Deployed process process-starter from resource bpmn/process-starter.bpmn with key 2251799813685249 and version 1
    22:36:09.858 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Deployed process smoke-test from resource bpmn/smoke-test.bpmn with key 2251799813685250 and version 1
    22:36:09.858 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Deployed process task-test from resource bpmn/task-test.bpmn with key 2251799813685251 and version 1
    22:36:09.858 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Deployed process timer-test from resource bpmn/timer-test.bpmn with key 2251799813685252 and version 1
    22:36:10.022 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Started process instance with key 2251799813702527 for process name smoke-test with instances 100
    22:36:10.090 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Started process instance with key 4503599627386295 for process name task-test with instances 100
    22:36:10.594 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Started process instance with key 2251799813702961 for process name timer-test with instances 100
    22:36:10.636 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - Waiting for process instances to finish
    22:37:54.216 [org.menski.Yacqt.main()] INFO  org.menski.Yacqt - All process instances finished
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  02:03 min
    [INFO] Finished at: 2022-02-09T22:38:09+01:00
    [INFO] ------------------------------------------------------------------------
    ```
