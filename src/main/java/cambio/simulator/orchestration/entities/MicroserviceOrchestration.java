package cambio.simulator.orchestration.entities;

import cambio.simulator.entities.microservice.*;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import desmoj.core.simulator.Model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MicroserviceOrchestration extends Microservice {

    LoadBalancerOrchestration loadBalancerOrchestration;
    private int startTime = 0;

    /**
     * Creates a new instance of a {@link Microservice}.
     */
    public MicroserviceOrchestration(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public MicroserviceInstance getNextAvailableInstance() throws NoInstanceAvailableException {
        //TraceNote For debugging purposes
        //sendTraceNote("Finding next instance using " + loadBalancerOrchestration.getPlainName());
        return loadBalancerOrchestration.getNextServiceInstance();
    }


    @Override
    public synchronized void killInstance() {
        MicroserviceInstance instanceToKill = instancesSet.stream().filter(microserviceInstance -> microserviceInstance.getState().equals(InstanceState.RUNNING)).findFirst().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        Container containerForMicroServiceInstance = ManagementPlane.getInstance().getContainerForMicroServiceInstance(instanceToKill);
        containerForMicroServiceInstance.die();
    }

    public MicroserviceInstance createMicroServiceInstance() {
        MicroserviceInstance changedInstance;
        changedInstance = new MicroserviceOrchestrationInstance(getModel(), String.format("[%s]_I%d", getName(), instanceSpawnCounter), this.traceIsOn(), this, instanceSpawnCounter);
        changedInstance.activatePatterns(instanceOwnedPatternConfigurations);

        instanceSpawnCounter++;
        instancesSet.add(changedInstance);
        return changedInstance;
    }

    public Deployment getDeployment() {
        for (Deployment deployment : ManagementPlane.getInstance().getDeployments()) {
            if (deployment.getService() != null && deployment.getService().equals(this)) {
                return deployment;
            }
        }
        return null;
    }

}
