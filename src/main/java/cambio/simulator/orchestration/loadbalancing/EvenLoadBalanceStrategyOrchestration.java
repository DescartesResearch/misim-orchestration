package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Pod;

import java.util.*;
import java.util.stream.Collectors;

public class EvenLoadBalanceStrategyOrchestration implements IOrchestrationLoadBalancingStrategy {
    private Map<Pod, Integer> distribution = new HashMap<>();

    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) throws NoInstanceAvailableException {
        final Set<Pod> replicaSet = microserviceOrchestration.getDeployment().getRunningReplicas();
        if (distribution.keySet().size() != replicaSet.size()) {
            distribution = new HashMap<>(replicaSet.size());
            for (Pod pod : replicaSet) {
                distribution.put(pod, 0);
            }
        }

        List<Pod> sorted = distribution.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).collect(Collectors.toList());

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
