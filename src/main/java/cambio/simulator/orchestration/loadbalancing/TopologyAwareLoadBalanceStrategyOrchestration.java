package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import java.util.*;
import java.util.stream.Stream;

public class TopologyAwareLoadBalanceStrategyOrchestration implements IOrchestrationLoadBalancingStrategy {

    Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());

    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances, Request request)
            throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration, Request request)
            throws NoInstanceAvailableException {
        final Set<Pod> replicas = microserviceOrchestration.getDeployment().getRunningReplicas();
        List<Pod> replicaList = new ArrayList<>(replicas);
        Collections.shuffle(replicaList, random);

        // Try to find a Pod in the same zone, if a `requester` is set
        if (request.getRequester() != null) {
            ManagementPlane managementPlane = ManagementPlane.getInstance();
            Container sourceContainer = managementPlane
                    .getContainerForMicroServiceInstance(request.getRequester());
            Pod sourcePod = managementPlane.getPodForContainer(sourceContainer);
            String sourceZone = getZoneForPod(sourcePod);
            if (sourceZone != null) {
                Stream<Pod> sameZonePods = replicaList.stream()
                        .filter(p -> Objects
                                .equals(getZoneForPod(p), sourceZone));
                MicroserviceInstance sameZoneInstance = getAvailableInstances(sameZonePods, microserviceOrchestration)
                        .findFirst().orElse(null);
                if (sameZoneInstance != null)
                    return sameZoneInstance;
            }
        }

        // Fallback to random selection if:
        // - no requester exists,
        // - no zone label exists, or
        // - no Pod is available in the zone
        return getAvailableInstances(replicaList.stream(), microserviceOrchestration).findFirst()
                .orElseThrow(NoInstanceAvailableException::new);

    }

    private Stream<MicroserviceInstance> getAvailableInstances(Stream<Pod> pods,
            MicroserviceOrchestration microserviceOrchestration) {
        return pods.flatMap(p -> p.getContainers().stream())
                .filter(c -> microserviceOrchestration.equals(c.getMicroserviceInstance().getOwner())
                        && c.getContainerState() == ContainerState.RUNNING)
                .map(Container::getMicroserviceInstance);
    }

    private String getZoneForPod(Pod pod) {
        return Optional.ofNullable(pod).map(Pod::getLastKnownNode).map(Node::getKubernetesRepresentation)
                .map(V1Node::getMetadata).map(V1ObjectMeta::getLabels)
                .map(labels -> labels.get("topology.kubernetes.io/zone")).orElse(null);
    }
}
