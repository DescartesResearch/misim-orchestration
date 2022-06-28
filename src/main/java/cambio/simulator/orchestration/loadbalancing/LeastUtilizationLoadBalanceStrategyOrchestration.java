package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;

import java.util.*;

public class LeastUtilizationLoadBalanceStrategyOrchestration implements IOrchestrationLoadBalancingStrategy {
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances) throws NoInstanceAvailableException {
        throw new UnsupportedOperationException("Not supposed to be called in orchestration mode");
    }

    @Override
    public MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) {
        final Set<Pod> replicaSet = microserviceOrchestration.getDeployment().getRunningReplicas();
        MicroserviceInstance leastUtilized = null;
        for (Pod pod : replicaSet) {
            final Set<Container> containers = pod.getContainers();
            for (Container container : containers) {
                if (container.getMicroserviceInstance().getOwner().equals(microserviceOrchestration)) {
                    if (container.getContainerState() == ContainerState.RUNNING) {
                        if (leastUtilized == null || container.getMicroserviceInstance().getRelativeWorkDemand() < leastUtilized.getRelativeWorkDemand()) {
                            leastUtilized = container.getMicroserviceInstance();
                        }
                    }
                    break;
                }
            }
        }
        return leastUtilized;
    }
}
