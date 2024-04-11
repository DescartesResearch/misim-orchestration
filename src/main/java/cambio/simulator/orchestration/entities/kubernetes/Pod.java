package cambio.simulator.orchestration.entities.kubernetes;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Pod extends NamedEntity {

    @Getter
    @Setter
    private PodState podState;

    @Getter
    @Setter
    private Set<Container> containers;

    @Getter
    @Setter
    private Node lastKnownNode;

    @Getter
    private final Deployment owner;
    private final double cpuDemand;
    @Getter
    @Setter
    private V1Pod kubernetesRepresentation;

    public Pod(Model model, String name, boolean showInTrace, Deployment deployment, double cpuDemandCores) {
        super(model, name, showInTrace);
        this.containers = new HashSet<>();
        this.podState = PodState.PENDING;
        this.owner = deployment;
        this.cpuDemand = cpuDemandCores;
    }

    public double getCPUDemand() {
        return cpuDemand;
    }

    public void die() {
        transitionToState(PodState.FAILED);
    }

    public void startAllContainers() {
        transitionToState(PodState.RUNNING);
    }

//    /**
//     * Should be called when a Pod has died due to a ChaosMonkeyForPodsEvents.
//     * It restarts all containers that belong to this pod.
//     */
//    public void restartAllContainers() {
//        containers.forEach(container -> container.restartTerminatedContainer());
//    }

    public String getSchedulerName() {
        return Optional.ofNullable(owner).map(Deployment::getSchedulerType).map(SchedulerType::getName).orElse("N/A");
    }

    public void transitionToState(PodState podState) {
        this.podState = podState;
        if (podState == PodState.TERMINATING && owner.getService() != null) {
            getContainers().forEach(container -> container.getMicroserviceInstance().startShutdown());
        } else if (podState == PodState.SUCCEEDED) {
            getContainers().forEach(container -> container.setContainerState(ContainerState.TERMINATED));
        } else if (podState == PodState.FAILED) {
            getContainers().forEach(Container::die);
        } else if (podState == PodState.RUNNING) {
            List<Container> collect =
                    getContainers().stream().filter(container -> !container.getContainerState().equals(ContainerState.RUNNING)).collect(Collectors.toList());
            collect.forEach(Container::start);
        }
    }

    public void bindToNode(String nodeName) {
        kubernetesRepresentation.getSpec().setNodeName(nodeName);
    }
}
