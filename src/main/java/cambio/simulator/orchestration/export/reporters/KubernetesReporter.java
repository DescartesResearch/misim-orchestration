package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.export.CSVBuilder;
import cambio.simulator.orchestration.export.Stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.*;


public class KubernetesReporter {
    public static void generateKubernetesReports(Path reportPath) {
        // Scaling report
        Path scalingPath = reportPath.resolve("scaling");
        generateScalingReport(scalingPath);

        // Scheduling-related reports
        Path schedulingPath = reportPath.resolve("scheduling");
        generateSchedulingReport(schedulingPath);
        Path nodesAndPodsPath = schedulingPath.resolve("nodes_and_pods");
        generateNodesAndPodsReport(nodesAndPodsPath);
        Path nodePodSchedulerEventPath = schedulingPath.resolve("node_pod_scheduler_events.csv");
        generateNodePodSchedulerEventReport(nodePodSchedulerEventPath);
    }

    private static void generateScalingReport(Path scalingPath) {
        Map<Deployment, List<Stats.ScalingRecord>> deploymentRecordsMap = Stats.getInstance().getDeploymentRecordsMap();
        for (Deployment deployment : deploymentRecordsMap.keySet()) {
            List<Stats.ScalingRecord> scalingRecords = deploymentRecordsMap.get(deployment);
            String deploymentCsvFileName = deployment.getPlainName() + ".csv";
            Path csvPath = scalingPath.resolve(deploymentCsvFileName);


            CSVBuilder csvBuilder = new CSVBuilder();
            List<String> headers = new ArrayList<>();
            headers.add("Time");
            headers.add("AvgConsumption");
            headers.add("#Pods");

            List<Pod> pods = new ArrayList<>();
            if (!scalingRecords.isEmpty()) {
                pods = new ArrayList<>(deployment.getReplicaSet());
                pods.sort(Comparator.comparing(p -> p.getQuotedName().split("#")[1]));
                for (Pod pod : pods) {
                    headers.add(pod.getQuotedName());
                }
            }

            csvBuilder.headers(headers);

            for (Stats.ScalingRecord scalingRecord : scalingRecords) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(scalingRecord.getTime()));
                row.add(String.valueOf(scalingRecord.getAvgConsumption()));
                row.add(String.valueOf(scalingRecord.getAmountPods()));
                for (Pod pod : pods) {
                    row.add(String.valueOf(scalingRecord.getPodDoubleHashMap().get(pod)));
                }


                csvBuilder.row(row);
            }
            try {
                csvBuilder.build(csvPath);
            } catch (CSVBuilder.CSVBuilderException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    private static void generateSchedulingReport(Path schedulingPath) {
        List<Stats.SchedulingRecord> schedulingRecords = Stats.getInstance().getSchedulingRecords();
        String csvFileName = "scheduling_results.csv";
        Path csvPath = schedulingPath.resolve(csvFileName);

        CSVBuilder csvBuilder = new CSVBuilder();
        csvBuilder.headers(Arrays.asList("Time", "Capacity", "Reserved", "#PodsOnNodes", "#PodsWaiting",
                "PercentageScheduledPods"));

        for (Stats.SchedulingRecord schedulingRecord : schedulingRecords) {
            List<String> row = Arrays.asList(String.valueOf(schedulingRecord.getTime()),
                    String.valueOf(schedulingRecord.getCapacityTogether()),
                    String.valueOf(schedulingRecord.getReservedTogether()),
                    String.valueOf(schedulingRecord.getAmountPodsOnNodes()),
                    String.valueOf(schedulingRecord.getAmountPodsWaiting()),
                    String.valueOf(getPctScheduledPods(schedulingRecord)));
            csvBuilder.row(row);
        }
        try {
            csvBuilder.build(csvPath);
        } catch (CSVBuilder.CSVBuilderException e) {
            System.err.println(e.getMessage());
        }
    }

    private static double getPctScheduledPods(Stats.SchedulingRecord schedulingRecord) {
        int podsOnNodes = schedulingRecord.getAmountPodsOnNodes();
        int totalPods = podsOnNodes + schedulingRecord.getAmountPodsWaiting();
        if (totalPods == 0) return 1.0;
        double result = (double) podsOnNodes / totalPods;
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    private static void generateNodesAndPodsReport(Path nodesAndPodsPath) {
        Map<Node, List<Stats.NodePodSchedulingRecord>> node2PodMap = Stats.getInstance().getNode2PodMap();
        for (Node node : node2PodMap.keySet()) {
            List<Stats.NodePodSchedulingRecord> nodePodSchedulingRecords = node2PodMap.get(node);
            Path csvPath = nodesAndPodsPath.resolve(node.getPlainName() + "_results.csv");

            CSVBuilder csvBuilder = new CSVBuilder();

            // Add headers
            List<String> headers = new ArrayList<>();
            headers.add("Time");
            if (!nodePodSchedulingRecords.isEmpty()) {
                Stats.NodePodSchedulingRecord nodePodSchedulingRecord = nodePodSchedulingRecords.get(0);
                Map<Deployment, Integer> deploymentPodScheduledMap =
                        nodePodSchedulingRecord.getDeploymentPodScheduledMap();
                List<Deployment> deployments = new ArrayList<>(deploymentPodScheduledMap.keySet());
                for (Deployment deployment : deployments) {
                    headers.add(deployment.getPlainName());
                }

                csvBuilder.headers(headers);

                for (Stats.NodePodSchedulingRecord podSchedulingRecord : nodePodSchedulingRecords) {
                    List<String> row = new ArrayList<>();
                    row.add(String.valueOf(podSchedulingRecord.getTime()));

                    for (Deployment deployment : deployments) {
                        Integer count = podSchedulingRecord.getDeploymentPodScheduledMap().get(deployment);
                        row.add(String.valueOf(count));
                    }

                    csvBuilder.row(row);
                }
            }
            try {
                csvBuilder.build(csvPath);
            } catch (CSVBuilder.CSVBuilderException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    private static void generateNodePodSchedulerEventReport(Path nodePodSchedulerEventPath) {
        //### Get Node Pod Scheduler Event Report
        List<Stats.NodePodEventRecord> nodePodEventRecords = Stats.getInstance().getNodePodEventRecords();

        // Create an instance of CSVBuilder
        CSVBuilder csvBuilder = new CSVBuilder();

        // Set headers
        List<String> headers = Arrays.asList("Time", "desiredDeplState", "currentDeplStateOnNode", "Pod", "Node",
                "Scheduler", "Event", "Status", "Details");
        csvBuilder.headers(headers);

        // Add rows
        for (Stats.NodePodEventRecord nodePodEventRecord : nodePodEventRecords) {
            List<String> row = Arrays.asList(String.valueOf(nodePodEventRecord.getTime()),
                    String.valueOf(nodePodEventRecord.getDesiredState()),
                    String.valueOf(nodePodEventRecord.getCurrentState()),
                    String.valueOf(nodePodEventRecord.getPodName()), String.valueOf(nodePodEventRecord.getNodeName())
                    , String.valueOf(nodePodEventRecord.getScheduler()),
                    String.valueOf(nodePodEventRecord.getEvent()), String.valueOf(nodePodEventRecord.getOutcome()),
                    String.valueOf(nodePodEventRecord.getInfo()));
            csvBuilder.row(row);
        }
        try {
            csvBuilder.build(nodePodSchedulerEventPath);
        } catch (CSVBuilder.CSVBuilderException e) {
            System.err.println(e.getMessage());
        }

    }


}
