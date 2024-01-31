package cambio.simulator.orchestration.management;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.entities.Cluster;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.orchestration.events.CheckPodRemovableEvent;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class ManagementPlane {
    @Getter
    List<Deployment> deployments;
    @Getter
    Cluster cluster;
    @Getter
    Model model;
    Map<SchedulerType, Scheduler> schedulerMap;
    Map<String, String> defaultValues;
    public int podsRemovedFromNode = 0;

    @Getter
    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane() {
        schedulerMap = new HashMap<>();
        deployments = new ArrayList<>();
        defaultValues = new HashMap<>();
    }


    /**
     * For each deployment, check if rescaling is required
     */
    public void checkForScaling() {
        deployments.forEach(Deployment::scale);
    }

    /**
     * For each deployment, check if desired state equals current state, if not trigger actions
     */
    public void maintainDeployments() {
        for (Deployment deployment : deployments) {
            deployment.deploy();
        }
    }

    /**
     * Each scheduler will try to schedule the pending pods that are waiting in its queue
     */
    public void checkForPendingPods() {
        schedulerMap.values().stream().sorted().forEach(Scheduler::schedulePods);

        String nodeInfo = "NAME | CPUAvail | CPURes | #pods";
        getModel().sendTraceNote(nodeInfo);
        for (Node node : getCluster().getNodes()) {
            getModel().sendTraceNote(node.getName() + " | " + node.getTotalCPU() + " | " + node.getReserved() + " | " + node.getPods().size());
        }

        getModel().sendTraceNote(nodeInfo);

    }

    public void addPodToSpecificSchedulerQueue(Pod pod, SchedulerType schedulerType) {
        final Scheduler scheduler = schedulerMap.get(schedulerType);
        if (scheduler != null) {
            scheduler.getPodWaitingQueue().add(pod);
        } else {
            try {
                throw new IllegalStateException("Scheduler type: " + schedulerType + " is not known in schedulerMap");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Check if a pod's containers have no calculations left. If so then remove it from the pod.
     * Otherwise this method schedules itself again for a later time
     */
    public void checkIfPodRemovableFromNode(Pod pod, Node node) {
        for (Container container : pod.getContainers()) {
            double relativeWorkDemand = (container.getMicroserviceInstance() != null ? container.getMicroserviceInstance().getRelativeWorkDemand() : 0);
            if (relativeWorkDemand > 0) {
                getModel().sendTraceNote("Cannot remove pod with " + container.getMicroserviceInstance().getName() + " because at least one container is still calculating. Current Relative WorkDemand: " + relativeWorkDemand);
                final CheckPodRemovableEvent checkPodRemovableEvent = new CheckPodRemovableEvent(getModel(), "Check if pod can be removed", getModel().traceIsOn());
                checkPodRemovableEvent.schedule(pod, node, new TimeSpan(2));
                return;
            }
        }
        removePodFromNode(pod, node);
    }

    public void removePodFromNode(Pod pod, Node node) {
        node.removePod(pod);
        podsRemovedFromNode++;
    }

    public void populateSchedulers() {
        final Set<SchedulerType> usedSchedulerTypes = deployments.stream().map(Deployment::getSchedulerType).collect(Collectors.toSet());
        usedSchedulerTypes.forEach(schedulerType -> {
            try {
                schedulerMap.put(schedulerType, Util.getInstance().getSchedulerInstanceByType(schedulerType));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        getModel().sendTraceNote("[INFO] Active Schedulers: " + schedulerMap.values().stream().sorted().collect(Collectors.toList()));

    }

    public Pod getPodByName(String name) {
        List<Pod> collect = deployments.stream().map(deployment -> new ArrayList<>(deployment.getReplicaSet())).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<Pod> first = collect.stream().filter(pod -> pod.getName().equals(name)).findFirst();
        return first.orElse(null);
    }

    /**
     * Returns all pods that are known by all nodes. That means they either are running or at least placed on the node
     * while waiting for being started
     *
     * @return
     */
    public List<Pod> getAllPodsPlacedOnNodes() {
        return cluster.getNodes().stream().map(node -> new ArrayList<>(node.getPods())).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public Pod getPodForContainer(Container container) {
        List<Pod> collect = deployments.stream().map(Deployment::getReplicaSet).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<Pod> first = collect.stream().filter(pod -> pod.getContainers().contains(container)).findFirst();
        return first.orElse(null);
    }

    public Container getContainerForMicroServiceInstance(MicroserviceInstance microserviceInstance) {
        Optional<Container> any = deployments.stream().map(Deployment::getReplicaSet).flatMap(Collection::stream).map(Pod::getContainers).flatMap(Collection::stream).filter(container -> container.getMicroserviceInstance() != null && container.getMicroserviceInstance().equals(microserviceInstance)).findAny();
        return any.orElse(null);

    }

    public int getAmountOfWaitingPods() {
        int amountPodsWaiting = 0;
        for (SchedulerType schedulerType : schedulerMap.keySet()) {
            Scheduler scheduler = schedulerMap.get(schedulerType);
            amountPodsWaiting += scheduler.getPodWaitingQueue().size();
        }
        return amountPodsWaiting;
    }

    public int getAmountOfPodsOnNodes(Deployment deployment){
        List<Pod> collect = new ArrayList<>(deployment.getReplicaSet());
        List<Pod> allPodsPlacedOnNodes = getAllPodsPlacedOnNodes();

        return (int) allPodsPlacedOnNodes.stream()
                .filter(collect::contains).count();

    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
        System.out.printf("[INFO] Created cluster with %d nodes\n", cluster.getNodes().size());
    }

    public int getExperimentSeed() {
        return ((MiSimModel) getModel()).getExperimentMetaData().getSeed();
    }

    public void setModel(Model model) {
        this.model = model;
    }

}
