package cambio.simulator.orchestration.entities.kubernetes;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scaling.AutoScaler;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Deployment extends NamedEntity {
    private MicroserviceOrchestration service;
    private Set<Pod> replicaSet;
    private SchedulerType schedulerType;
    private TimeInstant lastRescaling;
    private int desiredReplicaCount;
    private int maxReplicaCount;
    private int minReplicaCount;
    private double averageUtilization;
    private AutoScaler autoScaler;
    private String requestsCPU;
    private String requestsMem;
    private V1Deployment kubernetesRepresentation;

    public Deployment(Model model, String name, boolean showInTrace, MicroserviceOrchestration microserviceOrchestration, int desiredReplicaCount, SchedulerType schedulerType) {
        super(model, name, showInTrace);
        this.service = microserviceOrchestration;
        this.desiredReplicaCount = desiredReplicaCount;
        this.schedulerType = schedulerType;
        this.maxReplicaCount = 10;
        this.minReplicaCount = 1;
        this.averageUtilization = 50.0;
        this.lastRescaling = new TimeInstant(0);
        replicaSet = new HashSet<>();
    }

    public void deploy() {
        final int diff = Math.abs(getCurrentRunningOrPendingReplicaCount() - desiredReplicaCount);
        int i = 0;
        sendTraceNote("Checking state of deployment " + this.getQuotedPlainName());
        if (diff == 0) {
            sendTraceNote("no action needed");
        } else {
            while (i < diff) {
                i++;
                if (getCurrentRunningOrPendingReplicaCount() < desiredReplicaCount) {
                    createPod();
                } else {
                    removePod();
                }
            }
        }
    }

    public synchronized void createPod() {

        final Pod pod = new Pod(getModel(), "Pod-" + this.getPlainName(), traceIsOn(), this);
        final MicroserviceInstance microServiceInstance = service.createMicroServiceInstance();
        final Container container = new Container(getModel(), "Container[" + service.getPlainName() + "]", traceIsOn(), microServiceInstance);
        pod.getContainers().add(container);
        V1Pod v1Pod = createKubernetesPodWithTemplate("Pod-" + this.getPlainName());
        pod.setKubernetesRepresentation(v1Pod);
        replicaSet.add(pod);
        //add to specific scheduler queue
        addPodToWaitingQueue(pod);
    }

    private V1Pod createKubernetesPodWithTemplate(String name) {
        V1Pod result = new V1Pod();
        result.setApiVersion("v1");
        result.setKind("Pod");
        result.setMetadata(new V1ObjectMeta().name(name).namespace("default").uid(name));
        result.setSpec(kubernetesRepresentation.getSpec().getTemplate().getSpec());
        result.setStatus(new V1PodStatus().phase("Pending"));
        return result;
    }

    public void removePod() {
        final Optional<Pod> optionalPendingPod = replicaSet.stream().filter(pod -> pod.getPodState() == PodState.PENDING).findFirst();
        if (optionalPendingPod.isPresent()) {
            Pod pod = optionalPendingPod.get();
            //find corresponding waiting queue and remove pod there as well
            try {
                Scheduler scheduler = Util.getInstance().getSchedulerInstanceByType(this.schedulerType);
                scheduler.getPodWaitingQueue().remove(pod);
                this.getReplicaSet().remove(optionalPendingPod.get());
                sendTraceNote("A pending pod was removed from " + this.getPlainName() + " and from " + scheduler.getSchedulerType().getDisplayName());
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return;
        }

        final List<Pod> pods = replicaSet.stream().filter(pod -> pod.getPodState() == PodState.RUNNING).collect(Collectors.toList());
        if (pods.isEmpty()) {
            //Should not happen. If there is neither a pending nor a running pod, then this method should not have been called
            sendTraceNote("There is no pod that could be removed");
            return;
        }
        Pod podWithLeastConsumption = null;
        double podCPUUtilizationLeast = 0;
        for (Pod pod : pods) {
            double podCPUUtilization = 0;
            for (Container container : pod.getContainers()) {
                if (container.getContainerState() == ContainerState.RUNNING) {
                    double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                    podCPUUtilization += relativeWorkDemand;
                }
            }

            if (podWithLeastConsumption != null) {
                if (podCPUUtilization < podCPUUtilizationLeast) {
                    podCPUUtilizationLeast = podCPUUtilization;
                    podWithLeastConsumption = pod;
                }

            } else {
                podWithLeastConsumption = pod;
                podCPUUtilizationLeast = podCPUUtilization;
            }
        }
        final Pod podToRemove = podWithLeastConsumption;
        Node lastKnownNode = podToRemove.getLastKnownNode();
        if (lastKnownNode != null) {
            lastKnownNode.startRemovingPod(podToRemove);
        } else {
            throw new IllegalStateException("There is no node which knows the pod " + podToRemove.getQuotedPlainName());
        }
    }


    public synchronized void killPodInstances(final int numberOfInstances, final int retries, final String service) {
        final int maxKills = Math.max(0, Math.min(numberOfInstances, getRunningReplicas().size()));
        for (int i = 0; i < maxKills; i++) {
            killPodInstance(retries, service);
        }
    }

    /**
     * Kills a random instance. Can be called on a deployment that has 0 running instances.
     */
    public synchronized void killPodInstance(final int retries, final String service) {
        Pod instanceToKill =
                getRunningReplicas().stream().findFirst().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        if (service != null) {
            Optional<Container> any = instanceToKill.getContainers().stream().filter(container -> container.getMicroserviceInstance().getPlainName().contains(service)).findAny();
            if (any.isPresent()) {
                Container container = any.get();
                container.setRestartAttemptsLeft(retries);
                container.die();
            } else {
                sendTraceNote("AimingChaosMonkeyForPods did not find the microService with the name " + service + ". Did not kill mircoServiceInstance");
            }
        } else {
            instanceToKill.die();
        }

    }

    public void scale() {
        if (autoScaler != null) {
            autoScaler.apply(this);
        }
    }

    public void addPodToWaitingQueue(Pod pod) {
        ManagementPlane.getInstance().addPodToSpecificSchedulerQueue(pod, this.getSchedulerType());
    }

    public MicroserviceOrchestration getService() {
        return service;
    }

    public void setService(MicroserviceOrchestration service) {
        this.service = service;
    }

    public int getDesiredReplicaCount() {
        return desiredReplicaCount;
    }

    public void setDesiredReplicaCount(int desiredReplicaCount) {
        this.desiredReplicaCount = desiredReplicaCount;
    }

    public int getCurrentReplicaCount() {
        return getReplicaSet().size();
    }

    public int getCurrentRunningOrPendingReplicaCount() {
        return getCurrentRunningOrPendingReplicas().size();
    }

    public Set<Pod> getCurrentRunningOrPendingReplicas() {
        return getReplicaSet().stream().filter(pod -> pod.getPodState() == PodState.RUNNING || pod.getPodState() == PodState.PENDING).collect(Collectors.toSet());
    }

    public Set<Pod> getRunningReplicas() {
        return getReplicaSet().stream().filter(pod -> pod.getPodState() == PodState.RUNNING).collect(Collectors.toSet());
    }

    public Set<Pod> getReplicaSet() {
        return replicaSet;
    }

    public void setReplicaSet(Set<Pod> replicaSet) {
        this.replicaSet = replicaSet;
    }

    public SchedulerType getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(SchedulerType schedulerType) {
        this.schedulerType = schedulerType;
    }

    public int getMaxReplicaCount() {
        return maxReplicaCount;
    }

    public void setMaxReplicaCount(int maxReplicaCount) {
        this.maxReplicaCount = maxReplicaCount;
    }

    public int getMinReplicaCount() {
        return minReplicaCount;
    }

    public void setMinReplicaCount(int minReplicaCount) {
        this.minReplicaCount = minReplicaCount;
    }

    public TimeInstant getLastRescaling() {
        return lastRescaling;
    }

    public void setLastRescaling(TimeInstant lastRescaling) {
        this.lastRescaling = lastRescaling;
    }

    public double getAverageUtilization() {
        return averageUtilization;
    }

    public void setAverageUtilization(double averageUtilization) {
        this.averageUtilization = averageUtilization;
    }

    public AutoScaler getAutoScaler() {
        return autoScaler;
    }

    public void setAutoScaler(AutoScaler autoScaler) {
        this.autoScaler = autoScaler;
    }

    public String getRequestsCPU() {
        return requestsCPU;
    }

    public void setRequestsCPU(String requestsCPU) {
        this.requestsCPU = requestsCPU;
    }

    public String getRequestsMem() {
        return requestsMem;
    }

    public void setRequestsMem(String requestsMem) {
        this.requestsMem = requestsMem;
    }

    public V1Deployment getKubernetesRepresentation() {
        return kubernetesRepresentation;
    }

    public void setKubernetesRepresentation(V1Deployment kubernetesRepresentation) {
        this.kubernetesRepresentation = kubernetesRepresentation;
    }
}
