package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.*;
import java.util.stream.Collectors;

public class TopologyAwareLoadBalanceStrategyOrchestration implements IOrchestrationLoadBalancingStrategy {

    Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());

    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances, Request request) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration, Request request) throws NoInstanceAvailableException {
        final Set<Pod> replicaSet = microserviceOrchestration.getDeployment().getRunningReplicas();
        List<Pod> sorted;
        if (request.getRequester() == null) {
            sorted = new ArrayList<>(replicaSet);
            Collections.shuffle(sorted, random);
        } else {
            Container sourceContainer = ManagementPlane.getInstance().getContainerForMicroServiceInstance(request.getRequester());
            Pod sourcePod = ManagementPlane.getInstance().getPodForContainer(sourceContainer);
            String sourceNodeName = sourcePod.getLastKnownNode().getPlainName();
            sorted = replicaSet.stream()
                    .sorted(Comparator.comparingDouble(o -> ManagementPlane.getInstance().getCluster().getNetworkDelay(sourceNodeName, o.getLastKnownNode().getPlainName())))
                    .collect(Collectors.toList());
        }

        for (Pod pod : sorted) {
            final Set<Container> containers = pod.getContainers();
            for (Container container : containers) {
                if (container.getMicroserviceInstance().getOwner().equals(microserviceOrchestration)) {
                    if (container.getContainerState() == ContainerState.RUNNING) {
                        return container.getMicroserviceInstance();
                    }
                }
            }
        }
        return null;
    }
}

