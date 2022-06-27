package cambio.simulator.orchestration.adapters;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.orchestration.MicroserviceOrchestration;

public interface IOrchestrationLoadBalancingStrategy extends ILoadBalancingStrategy {
    /**
     * In case we use the orchestration plugin we dont have a collection of microservice instances, we have a MicroserviceOrchestration
     * (like we have in Kubernetes for abstracting network access to a group of instances). default method just for
     * compatability with old version. Inside the orchestration plugin, only this method is used. Other method throws
     * UnsupportedOperationException if used in orchestration mode.
     * @param microserviceOrchestration
     * @return
     * @throws NoInstanceAvailableException
     */
    default MicroserviceInstance getNextInstance(MicroserviceOrchestration microserviceOrchestration) throws
            NoInstanceAvailableException {
        return null;
    }
}
