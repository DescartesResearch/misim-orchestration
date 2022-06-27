package cambio.simulator.orchestration.loadbalancing;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.adapters.IOrchestrationLoadBalancingStrategy;
import desmoj.core.simulator.Model;

public class LoadBalancerOrchestration extends NamedEntity {
    private final IOrchestrationLoadBalancingStrategy loadBalancingStrategy;
    private final MicroserviceOrchestration microserviceOrchestration;

    public LoadBalancerOrchestration(Model model, String name, boolean showInTrace, IOrchestrationLoadBalancingStrategy loadBalancingStrategy, MicroserviceOrchestration microserviceOrchestration) {
        super(model, name, showInTrace);
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.microserviceOrchestration = microserviceOrchestration;
    }

    public MicroserviceInstance getNextServiceInstance() {
        return loadBalancingStrategy.getNextInstance(this.microserviceOrchestration);
    }
}
