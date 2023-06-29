package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Pod;

import java.util.*;

public class RoundRobinLoadBalanceStrategyOrchestration implements IOrchestrationLoadBalancingStrategy {
    Deque<Pod> queue = new ArrayDeque<>();
    HashSet<Pod> queued = new HashSet<>();

    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) {
        final Set<Pod> replicaSet = microserviceOrchestration.getDeployment().getRunningReplicas();

        replicaSet.forEach(pod -> {
            if (!queued.contains(pod)) {
                queue.addFirst(pod);
                queued.add(pod);
            }
        });

        MicroserviceInstance selected = null;
        Deque<Pod> seenPods = new ArrayDeque<>();

        while (selected == null && !queue.isEmpty()) {
            Pod pod = queue.poll();
            final Set<Container> containers = pod.getContainers();
            for (Container container : containers) {
                if (container.getMicroserviceInstance().getOwner().equals(microserviceOrchestration)) {
                    if (container.getContainerState() == ContainerState.RUNNING) {
                        selected = container.getMicroserviceInstance();
                    }
                    break;
                }
            }
            seenPods.addLast(pod);
        }
        while (!seenPods.isEmpty()) {
            queue.addLast(seenPods.poll());
        }
        assert queue.size() == queued.size();
        return selected;
    }
}
