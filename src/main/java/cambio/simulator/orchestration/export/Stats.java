package cambio.simulator.orchestration.export;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.entities.kubernetes.PodState;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.rest.dto.BindingFailureInformation;
import cambio.simulator.orchestration.rest.dto.BindingInformation;
import desmoj.core.simulator.Model;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Stats {
    //only for MiSim
    Map<Microservice, List<ScalingRecord>> microServiceRecordsMap = new HashMap<>();

    //only in orchestration mode
    Map<Deployment, List<ScalingRecord>> deploymentRecordsMap = new HashMap<>();
    List<SchedulingRecord> schedulingRecords = new ArrayList<>();
    Map<Node, List<NodePodSchedulingRecord>> node2PodMap = new HashMap<>();

    List<NodePodEventRecord> nodePodEventRecords = new ArrayList<>();

    @Getter
    @Setter
    public static class NodePodEventRecord {
        private int time;
        private String podName;
        private String nodeName;
        private String scheduler;
        private String event;
        private String outcome;
        private String info;
        private int desiredState;
        private int currentState;

        private NodePodEventRecord(Builder builder) {
            this.time = builder.time;
            this.podName = builder.podName;
            this.nodeName = builder.nodeName;
            this.scheduler = builder.scheduler;
            this.event = builder.event;
            this.outcome = builder.outcome;
            this.info = builder.info;
            this.desiredState = builder.desiredState;
            this.currentState = builder.currentState;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int time;
            private String podName;
            private String nodeName;
            private String scheduler;
            private String event;
            private String outcome;
            private String info;
            private int desiredState;
            private int currentState;


            public Builder fromBindingInformation(BindingInformation bindSuccess) {
                this.podName = bindSuccess.getPod();
                this.nodeName = bindSuccess.getNode();
                this.scheduler = "kube"; // in case of binding failure, it is always the kube scheduler
                this.event = "Binding";
                this.outcome = "Success";
                this.info = "N/A";
                return this;
            }

            public Builder fromBindingFailureInformation(BindingFailureInformation bindFail) {
                this.podName = bindFail.getPod();
                this.nodeName = "N/A"; // Fail, so not bound to a node
                this.scheduler = "kube"; // in case of binding failure, it is always the kube scheduler
                this.event = "Binding";
                this.outcome = "Failed";
                this.info = bindFail.getMessage();
                return this;
            }


            public Builder time(int time) {
                this.time = time;
                return this;
            }

            public Builder podName(String podName) {
                this.podName = podName;
                return this;
            }

            public Builder nodeName(String nodeName) {
                this.nodeName = nodeName;
                return this;
            }

            public Builder scheduler(String scheduler) {
                this.scheduler = scheduler;
                return this;
            }

            public Builder event(String event) {
                this.event = event;
                return this;
            }

            public Builder outcome(String outcome) {
                this.outcome = outcome;
                return this;
            }

            public Builder info(String info) {
                this.info = info;
                return this;
            }

            public Builder desiredState(int desiredState) {
                this.desiredState = desiredState;
                return this;
            }

            public Builder currentState(int currentState) {
                this.currentState = currentState;
                return this;
            }

            public NodePodEventRecord build() {
                return new NodePodEventRecord(this);
            }
        }
    }

    @Getter
    @Setter
    public static class NodePodSchedulingRecord {
        int time;
        Map<Deployment, Integer> deploymentPodScheduledMap = new HashMap<>();
    }

    @Getter
    @Setter
    public static class SchedulingRecord {
        int time;
        double capacityTogether;
        double reservedTogether;
        int amountPodsOnNodes;
        int amountPodsWaiting;
    }

    @Getter
    @Setter
    public static class ScalingRecord {
        int time;
        double avgConsumption;
        int amountPods;
        Map<Microservice, Integer> microserviceCanceledMap = new HashMap<>();
        Map<Pod, Double> podDoubleHashMap = new HashMap<>();
        Map<MicroserviceInstance, Double> microserviceInstanceDoubleHashMap = new HashMap<>();

        public ScalingRecord() {
        }
    }

    @Getter
    private static final Stats instance = new Stats();

    //private constructor to avoid client applications to use constructor
    private Stats() {
    }

    public void createSchedulingStats(Model model) {
        int time = (int) model.presentTime().getTimeAsDouble();
        Stats.SchedulingRecord schedulingRecord = new SchedulingRecord();
        schedulingRecord.setTime(time);
        double totalCPU = 0;
        double toalReserved = 0;
        for (Node node : ManagementPlane.getInstance().getCluster().getNodes()) {
            totalCPU += node.getTotalCPU();
            toalReserved += node.getReserved();
        }

        schedulingRecord.setCapacityTogether(totalCPU);
        schedulingRecord.setReservedTogether(toalReserved);
        schedulingRecord.setAmountPodsOnNodes(ManagementPlane.getInstance().getAllPodsPlacedOnNodes().size());
        schedulingRecord.setAmountPodsWaiting(ManagementPlane.getInstance().getAmountOfWaitingPods());
        schedulingRecords.add(schedulingRecord);

        //Add node2pod records


        List<Deployment> deployments = ManagementPlane.getInstance().getDeployments();
        List<Node> nodes = ManagementPlane.getInstance().getCluster().getNodes();
        for (Node node : nodes) {
            NodePodSchedulingRecord nodePodSchedulingRecord = new NodePodSchedulingRecord();
            nodePodSchedulingRecord.setTime(time);
            Map<Deployment, Integer> deploymentPodScheduledMap = nodePodSchedulingRecord.getDeploymentPodScheduledMap();

            for (Deployment deployment : deployments) {
                deploymentPodScheduledMap.put(deployment, 0);
            }

            for (Pod pod : node.getPods()) {
                Deployment deploymentForPod = pod.getOwner();
                if (deploymentForPod != null) {
                    if (deployments.contains(deploymentForPod)) {
                        if (deploymentPodScheduledMap.get(deploymentForPod) != null) {
                            deploymentPodScheduledMap.put(deploymentForPod,
                                    deploymentPodScheduledMap.get(deploymentForPod) + 1);
                        }
                    }
                }
            }

            List<NodePodSchedulingRecord> nodePodSchedulingRecords = node2PodMap.get(node);
            if (nodePodSchedulingRecords != null) {
                nodePodSchedulingRecords.add(nodePodSchedulingRecord);
            } else {
                ArrayList<NodePodSchedulingRecord> schedulingRecords = new ArrayList<>();
                schedulingRecords.add(nodePodSchedulingRecord);
                node2PodMap.put(node, schedulingRecords);
            }
        }


    }

    public void createScalingStats(Model model) {
        int time = (int) model.presentTime().getTimeAsDouble();

        List<Deployment> deployments = ManagementPlane.getInstance().getDeployments();
        for (Deployment deployment : deployments) {
            List<Double> podConsumptions = new ArrayList<>();

            Stats.ScalingRecord scalingRecord = new Stats.ScalingRecord();
            scalingRecord.setTime(time);

            for (Pod pod : deployment.getRunningReplicas()) {
                if (pod.getPodState() == PodState.RUNNING) {
                    double podCPUUtilization = 0;

                    for (Container container : pod.getContainers()) {

/*                        //add event info timeoutEvent
                        Microservice owner = container.getMicroserviceInstance().getOwner();
                        Integer integer = NetworkRequestTimeoutEvent.getMicroserviceTimeoutMap().get(owner);
                        if (integer != null) {
                            scalingRecord.getMicroservicetimoutmap().put(owner, Integer.valueOf(integer));
                        } else {
                            scalingRecord.getMicroservicetimoutmap().put(owner, 0);
                        }*/

//                        //add event info canceledEvent
//                        owner = container.getMicroserviceInstance().getOwner();
//                        integer = microserviceCanceledMap.get(owner);
//                        if (integer != null) {
//                            scalingRecord.getMicroserviceCanceledMap().put(owner, Integer.valueOf(integer));
//                        } else {
//                            scalingRecord.getMicroserviceCanceledMap().put(owner, 0);
//                        }


                        if (container.getContainerState() == ContainerState.RUNNING && container.getMicroserviceInstance() != null) {
                            double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                            podCPUUtilization += relativeWorkDemand;
                        }
                    }
                    podConsumptions.add(podCPUUtilization);
                    scalingRecord.getPodDoubleHashMap().put(pod, podCPUUtilization);
                }
            }
            double avg = podConsumptions.stream().mapToDouble(d -> d).average().orElse(0);
//            sendTraceNote("Average for  " + deployment.getQuotedName() + " has the current work demand: " + avg);
            scalingRecord.setAvgConsumption(avg);
            scalingRecord.setAmountPods(deployment.getRunningReplicas().size());


            List<Stats.ScalingRecord> scalingRecords = deploymentRecordsMap.get(deployment);
            if (scalingRecords != null) {
                scalingRecords.add(scalingRecord);
            } else {
                ArrayList<Stats.ScalingRecord> scalingRecordList = new ArrayList<>();
                scalingRecordList.add(scalingRecord);
                deploymentRecordsMap.put(deployment, scalingRecordList);
            }
        }
    }
}
