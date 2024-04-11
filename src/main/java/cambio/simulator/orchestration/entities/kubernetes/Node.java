package cambio.simulator.orchestration.entities.kubernetes;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.events.CheckPodRemovableEvent;
import cambio.simulator.orchestration.events.HealthCheckEvent;
import cambio.simulator.orchestration.events.StartPodEvent;
import cambio.simulator.orchestration.export.Stats;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import io.kubernetes.client.openapi.models.V1Node;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Node extends NamedEntity {

    private static final String BASE_IP_ADDRESS = "192.168.49.";
    private final String nodeIpAddress;
    private static int IP_ADDRESS_COUNTER = 1;

    private final double totalCPU;
    private double reserved = 0;
    @Setter
    private List<Pod> pods;
    @Setter
    private V1Node kubernetesRepresentation;

    public Node(Model model, String name, boolean showInTrace, double totalCPU) {
        super(model, name, showInTrace);
        this.totalCPU = totalCPU;
        this.pods = new ArrayList<>();
        this.nodeIpAddress = BASE_IP_ADDRESS + IP_ADDRESS_COUNTER++;
    }

    public synchronized boolean addPod(Pod pod) {
        boolean notEnoughCPUAvailable = this.getReserved() + pod.getCPUDemand() > this.getTotalCPU();
        if (notEnoughCPUAvailable) return false;
        this.reserved += pod.getCPUDemand();
        pods.add(pod);
        final StartPodEvent startPodEvent = new StartPodEvent(getModel(), "StartPodEvent", traceIsOn());
        startPodEvent.schedule(pod, presentTime());
        pod.setLastKnownNode(this);
        return true;
    }

    public void startRemovingPod(Pod pod) {
        Stats.NodePodEventRecord record =
                Stats.NodePodEventRecord.builder().time((int) presentTime().getTimeAsDouble()).podName(pod.getName()).nodeName(this.getPlainName()).scheduler(pod.getSchedulerName()).event("Start Pod Removal").outcome("Initiating").info(pod.getName() + " needs to be removed from " + this.getPlainName()).desiredState(pod.getOwner().getDesiredReplicaCount()).currentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner())).build();
        Stats.getInstance().getNodePodEventRecords().add(record);
        pod.transitionToState(PodState.TERMINATING);
        final CheckPodRemovableEvent checkPodRemovableEvent = new CheckPodRemovableEvent(getModel(),
                "Check if pod " + "can be removed", traceIsOn());
        checkPodRemovableEvent.schedule(pod, this, new TimeSpan(0));
    }

    public void removePod(Pod pod) {
        pod.transitionToState(PodState.SUCCEEDED);
        this.reserved -= pod.getCPUDemand();
        if (!pods.contains(pod)) throw new PodDoesNotBelongToNodeException(pod, this);
        pods.remove(pod);
        Stats.NodePodEventRecord record =
                Stats.NodePodEventRecord.builder().time((int) presentTime().getTimeAsDouble()).podName(pod.getName()).nodeName(this.getPlainName()).scheduler(pod.getSchedulerName()).event("Pod Removal").outcome("Success").info(pod.getName() + " was removed from " + this.getPlainName()).desiredState(pod.getOwner().getDesiredReplicaCount()).currentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner())).build();
        Stats.getInstance().getNodePodEventRecords().add(record);
        sendTraceNote(pod.getQuotedName() + " was removed from " + this.getQuotedName());
        HealthCheckEvent healthCheckEvent = new HealthCheckEvent(getModel(), "HealthCheckEvent - After Scaling",
                traceIsOn());
        healthCheckEvent.schedule(new TimeSpan(HealthCheckEvent.delay));
    }

    private static class PodDoesNotBelongToNodeException extends IllegalArgumentException {
        public PodDoesNotBelongToNodeException(Pod pod, Node node) {
            super("Pod " + pod.getQuotedPlainName() + " does not belong to node " + node.getQuotedPlainName());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            return ((Node) obj).getQuotedName().equals(getQuotedName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getQuotedName().hashCode();
    }
}
