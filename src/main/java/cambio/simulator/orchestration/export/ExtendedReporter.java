package cambio.simulator.orchestration.export;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.NetworkRequestSendEvent;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.events.*;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.walk;

public class ExtendedReporter {
    public static void createReport(String currentRunName, MiSimModel model) {
        // CREATE ORCHESTRATION RECORD DIRECTORY

        String directoryName = "orchestration_records";
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        File directorySpecificRun = new File(directory.getPath() + "/" + currentRunName);
        if (!directorySpecificRun.exists()) {
            directorySpecificRun.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        } else {
            try {
                throw new FileAlreadyExistsException("File with the name " + currentRunName + " already exists");
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
                // System.exit(1);
            }
        }

        // COPY RUN SPECIFIC CONFIG FILES

        try {
            File directoryConfigFiles = new File(directorySpecificRun.getPath() + "/" + "configFiles");
            if (!directoryConfigFiles.exists()) {
                directoryConfigFiles.mkdir();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
            }

            File directoryMiSimStandardReportFiles = new File(directorySpecificRun.getPath() + "/" + "misim_standard_report");
            if (!directoryMiSimStandardReportFiles.exists()) {
                directoryMiSimStandardReportFiles.mkdir();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
            }

            // ArrayList<String> removeFilters = new ArrayList<>();
            // removeFilters.add("scheduler");

            // copyDirectory("orchestration", directoryConfigFiles.getPath(), removeFilters);
            copyDirectory("BasicExample", directoryConfigFiles.getPath(), Arrays.asList(""));
            copyDirectory(model.getExperimentMetaData().getReportLocation().toAbsolutePath().toString(), directoryMiSimStandardReportFiles.getPath(), Arrays.asList(""));

        } catch (IOException e) {
            e.printStackTrace();
        }

        // CREATE DATA FOR PERFORMANCE

        String directoryNamePerformance = "Performance";
        File directoryPerformance = new File(directorySpecificRun.getPath() + "/" + directoryNamePerformance);
        if (!directoryPerformance.exists()) {
            directoryPerformance.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        File csvOutputFilePerformance = new File(directoryPerformance.getPath() + "/" + "performance_results.csv");


        try (PrintWriter pw = new PrintWriter(csvOutputFilePerformance)) {
            ArrayList<String> content = new ArrayList<>();
            content.add("Setup_time");
            content.add("Experiment_time");
            content.add("Report_time");
            content.add("Execution_time");
            pw.println(convertToCSV(content));
            content.clear();
            ExperimentMetaData metaData = model.getExperimentMetaData();
            content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getSetupExecutionDuration())));
            content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getExperimentExecutionDuration())));
            content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getReportExecutionDuration())));
            content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getExecutionDuration())));
            pw.println(convertToCSV(content));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // ADD EVENTS CSV
        addEventsResult(directoryPerformance);

        if (model instanceof MiSimOrchestrationModel
                && ((MiSimOrchestrationModel) model).getOrchestrationConfig().isOrchestrate()) {
            createOrchestrationReport(directorySpecificRun);
        }
        createPureMiSimReport(directorySpecificRun);
    }

    private static void createPureMiSimReport(File lastDirectory) {

        String directoryNameMiSim = "pure_MiSim_records";
        File directoryMiSim = new File(lastDirectory.getPath() + "/" + directoryNameMiSim);
        if (!directoryMiSim.exists()) {
            directoryMiSim.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        //###Create Data for scaling###

        String directoryNameScaling = "Scaling";
        File directoryScaling = new File(directoryMiSim.getPath() + "/" + directoryNameScaling);
        if (!directoryScaling.exists()) {
            directoryScaling.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        Map<Microservice, List<Stats.ScalingRecord>> microServiceRecordsMap = Stats.getInstance().getMicroServiceRecordsMap();

        for (Microservice microservice : microServiceRecordsMap.keySet()) {
            List<Stats.ScalingRecord> scalingRecords = microServiceRecordsMap.get(microservice);
            File csvOutputFile = new File(directoryScaling.getPath() + "/" + microservice.getPlainName() + ".csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                ArrayList<String> content = new ArrayList<>();
                content.add("Time");
                content.add("AvgConsumption");
                content.add("#Instances");
                content.add("NetworkRequestTimeoutEvent_" + microservice.getPlainName());

                //Add individual cpuconsumption
                List<MicroserviceInstance> collect = new ArrayList<>(microservice.getInstancesSet());
                Collections.sort(collect, new Comparator<MicroserviceInstance>() {
                    @Override
                    public int compare(MicroserviceInstance microservice1, MicroserviceInstance t1) {
                        String ms1 = microservice1.getQuotedName().split("#")[1];
                        String ms2 = t1.getQuotedName().split("#")[1];
                        return ms1.compareTo(ms2);
                    }
                });

                for (MicroserviceInstance microserviceInstance : collect) {
                    content.add(String.valueOf(microserviceInstance.getQuotedName()));
                }


                pw.println(convertToCSV(content));
                for (Stats.ScalingRecord scalingRecord : scalingRecords) {
                    content.clear();
                    content.add(String.valueOf(scalingRecord.getTime()));
                    content.add(String.valueOf(scalingRecord.getAvgConsumption()));
                    content.add(String.valueOf(scalingRecord.getAmountPods()));

                    for (MicroserviceInstance microserviceInstance : collect) {
                        content.add(String.valueOf(scalingRecord.getMicroserviceInstanceDoubleHashMap().get(microserviceInstance)));
                    }
                    pw.println(convertToCSV(content));
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //###END of: Create Data for scaling###

    }

    private static void createOrchestrationReport(File lastDirectory) {

        String directoryNameOrchestration = "Orchestration";
        File directoryOrchestration = new File(lastDirectory.getPath() + "/" + directoryNameOrchestration);
        if (!directoryOrchestration.exists()) {
            directoryOrchestration.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }


        //###Create Data for scaling###

        String directoryNameScaling = "Scaling";
        File directoryScaling = new File(directoryOrchestration.getPath() + "/" + directoryNameScaling);
        if (!directoryScaling.exists()) {
            directoryScaling.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        Map<Deployment, List<Stats.ScalingRecord>> deploymentRecordsMap = Stats.getInstance().getDeploymentRecordsMap();

        for (Deployment deployment : deploymentRecordsMap.keySet()) {
            List<Stats.ScalingRecord> scalingRecords = deploymentRecordsMap.get(deployment);
            File csvOutputFile = new File(directoryScaling.getPath() + "/" + deployment.getPlainName() + ".csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                ArrayList<String> content = new ArrayList<>();
                content.add("Time");
                content.add("AvgConsumption");
                content.add("#Pods");

                List<Microservice> microservices = null;
                List<Pod> pods = null;
                if (scalingRecords.size() != 0) {

                    //Add individual cpuconsumption
                    pods = new ArrayList<>(deployment.getReplicaSet());
                    Collections.sort(pods, new Comparator<Pod>() {
                        @Override
                        public int compare(Pod pod, Pod t1) {
                            String numberPod1 = pod.getQuotedName().split("#")[1];
                            String numberPod2 = t1.getQuotedName().split("#")[1];
                            return numberPod1.compareTo(numberPod2);
                        }
                    });
                    for (Pod pod : pods) {
                        content.add(pod.getQuotedName());
                    }
                }

                pw.println(convertToCSV(content));

                for (Stats.ScalingRecord scalingRecord : scalingRecords) {
                    content.clear();
                    content.add(String.valueOf(scalingRecord.getTime()));
                    content.add(String.valueOf(scalingRecord.getAvgConsumption()));
                    content.add(String.valueOf(scalingRecord.getAmountPods()));
                    if (scalingRecords.size() != 0) {
//                        //Add containerWithTimeoutRequests
//                        if (microservices != null) {
//                            for (Microservice microservice : microservices) {
//                                content.add(String.valueOf(scalingRecord.getMicroservicetimoutmap().get(microservice)));
//                                content.add(String.valueOf(scalingRecord.getMicroserviceCanceledMap().get(microservice)));
//                            }
//                        }
                        //Add microServiceInstance individual cpuconsumption
                        if (pods != null) {
                            for (Pod pod : pods) {
                                content.add(String.valueOf(scalingRecord.getPodDoubleHashMap().get(pod)));

                            }
                        }

                    }
                    pw.println(convertToCSV(content));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //###END of: Create Data for scaling###

        //###Create Data for scheduling###
        String directoryNameScheduling = "Scheduling";
        File directoryScheduling = new File(directoryOrchestration.getPath() + "/" + directoryNameScheduling);
        if (!directoryScheduling.exists()) {
            directoryScheduling.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        List<Stats.SchedulingRecord> schedulingRecords = Stats.getInstance().getSchedulingRecords();

        File csvOutputFile = new File(directoryScheduling.getPath() + "/" + "scheduling_results.csv");

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            ArrayList<String> content = new ArrayList<>();
            content.add("Time");
            content.add("Capacity");
            content.add("Reserved");
            content.add("#PodsOnNodes");
            content.add("#PodsWaiting");
            content.add("PecentageScheduledPods");
            pw.println(convertToCSV(content));
            for (Stats.SchedulingRecord schedulingRecord : schedulingRecords) {
                content.clear();
                content.add(String.valueOf(schedulingRecord.getTime()));
                content.add(String.valueOf(schedulingRecord.getCapacityTogether()));
                content.add(String.valueOf(schedulingRecord.getReservedTogether()));
                content.add(String.valueOf(schedulingRecord.getAmountPodsOnNodes()));
                content.add(String.valueOf(schedulingRecord.getAmountPodsWaiting()));
                double divisor = (double) schedulingRecord.getAmountPodsOnNodes() + schedulingRecord.getAmountPodsWaiting();
                if(divisor!=0){
                    double pecentageScheduledPods = (double) schedulingRecord.getAmountPodsOnNodes() / divisor;
                    pecentageScheduledPods = BigDecimal.valueOf(pecentageScheduledPods)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    content.add(String.valueOf(pecentageScheduledPods));
                }else{
                    content.add(String.valueOf(1));
                }
                pw.println(convertToCSV(content));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        //### Get Node and Pod distribution

        String directoryNameSchedulingNodes = "NodesAndPods";
        File directorySchedulingNodes = new File(directoryScheduling.getPath() + "/" + directoryNameSchedulingNodes);
        if (!directorySchedulingNodes.exists()) {
            directorySchedulingNodes.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        Map<Node, List<Stats.NodePodSchedulingRecord>> node2PodMap = Stats.getInstance().getNode2PodMap();

        for (Node node : node2PodMap.keySet()) {
            List<Stats.NodePodSchedulingRecord> nodePodSchedulingRecords = node2PodMap.get(node);

            File csvOutputFile4Node = new File(directorySchedulingNodes.getPath() + "/" + node.getPlainName() + "_results.csv");

            try (PrintWriter pw = new PrintWriter(csvOutputFile4Node)) {
                ArrayList<String> content = new ArrayList<>();
                content.add("Time");

                if (!nodePodSchedulingRecords.isEmpty()) {
                    Stats.NodePodSchedulingRecord nodePodSchedulingRecord = nodePodSchedulingRecords.get(0);
                    Map<Deployment, Integer> deploymentPodScheduledMap = nodePodSchedulingRecord.getDeploymentPodScheduledMap();
                    List<Deployment> deployments = new ArrayList<>(deploymentPodScheduledMap.keySet());
                    for (Deployment deployment : deployments) {
                        content.add(deployment.getPlainName());
                    }

                    pw.println(convertToCSV(content));
                    for (Stats.NodePodSchedulingRecord podSchedulingRecord : nodePodSchedulingRecords) {
                        content.clear();
                        content.add(String.valueOf(podSchedulingRecord.getTime()));

                        for (Deployment deployment : deployments) {
                            Integer integer = podSchedulingRecord.getDeploymentPodScheduledMap().get(deployment);
                            content.add(String.valueOf(integer));
                        }

                        pw.println(convertToCSV(content));
                    }

                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        //### Get Node Pod Scheduler Event Report
        List<Stats.NodePodEventRecord> nodePodEventRecords = Stats.getInstance().getNodePodEventRecords();

        File csvOutputFile4NodePodSchedulerEventReport = new File(directoryScheduling.getPath() + "/" + "Node_Pod_Scheduler_Event_results.csv");

        try (PrintWriter pw = new PrintWriter(csvOutputFile4NodePodSchedulerEventReport)) {
            ArrayList<String> content = new ArrayList<>();
            content.add("Time");
            content.add("desiredDeplState");
            content.add("currentDeplStateOnNode");
            content.add("Pod");
            content.add("Node");
            content.add("Scheduler");
            content.add("Event");
            content.add("Status");
            content.add("Details");
            pw.println(convertToCSV(content));
            for (Stats.NodePodEventRecord nodePodEventRecord : nodePodEventRecords) {
                content.clear();
                content.add(String.valueOf(nodePodEventRecord.getTime()));
                content.add(String.valueOf(nodePodEventRecord.getDesiredState()));
                content.add(String.valueOf(nodePodEventRecord.getCurrentState()));
                content.add(String.valueOf(nodePodEventRecord.getPodName()));
                content.add(String.valueOf(nodePodEventRecord.getNodeName()));
                content.add(String.valueOf(nodePodEventRecord.getScheduler()));
                content.add(String.valueOf(nodePodEventRecord.getEvent()));
                content.add(String.valueOf(nodePodEventRecord.getOutcome()));
                content.add(String.valueOf(nodePodEventRecord.getInfo()));
                pw.println(convertToCSV(content));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //###End of: Create Data for scheduling###
    }

    private static void addEventsResult(File pathName) {
        File csvOutputFileEvents = new File(pathName.getPath() + "/" + "events_results.csv");


        try (PrintWriter pw = new PrintWriter(csvOutputFileEvents)) {
            ArrayList<String> content = new ArrayList<>();
            //order: Ensure that titles are added before
            content.add("PodsRemovedFromNode");
            content.add("ScaleEvent");
            content.add("TryToRestartContainerEvent");
            content.add("RestartStartContainerAndMicroserviceInstanceEvent");
            content.add("StartContainerAndMicroserviceInstanceEvent");
            content.add("StartPodEvent");
//            HealthCheckEvent
            content.add("HealthCheckEvent");

            content.add("NetworkRequestSendEvent");
            content.add("OrchestrationEvents");
            content.add("MiSimEvents");
            content.add("Total");

            pw.println(convertToCSV(content));
            content.clear();
            //Orchestration Events
            content.add(String.valueOf(ManagementPlane.getInstance().podsRemovedFromNode));
            content.add(String.valueOf(ScaleEvent.counter));
            content.add(String.valueOf(TryToRestartContainerEvent.counter));
            content.add(String.valueOf(RestartStartContainerAndMicroserviceInstanceEvent.counter));
            content.add(String.valueOf(StartContainerAndMicroserviceInstanceEvent.counter));
            content.add(String.valueOf(StartPodEvent.counter));
            content.add(String.valueOf(HealthCheckEvent.counter));
            //count only those relevant for experiments (we neglect scaling)
            int sumOrchestationEvents = ScaleEvent.counter + StartContainerAndMicroserviceInstanceEvent.counter +
                    StartPodEvent.counter + HealthCheckEvent.counter;

            //Original Events
            content.add(String.valueOf(NetworkRequestSendEvent.getCounterSendEvents()));
            int sumOriginalEvents = (int) NetworkRequestSendEvent.getCounterSendEvents();

            //total
            content.add(String.valueOf(sumOrchestationEvents));
            content.add(String.valueOf(sumOriginalEvents));
            content.add(String.valueOf(sumOrchestationEvents + sumOriginalEvents));

            pw.println(convertToCSV(content));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation, List<String> removeFilters)
            throws IOException {
        walk(Paths.get(sourceDirectoryLocation)).filter(source -> !removeFilters.contains(source.getFileName().toString()))
                .forEach(source -> {
                    Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                            .substring(sourceDirectoryLocation.length()));
                    try {
                        copy(source, destination);
                    } catch (IOException e) {
//                        e.printStackTrace();
                    }
                });
    }

    private static String convertToCSV(List<String> data) {
        return data.stream()
                .map(ExtendedReporter::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
