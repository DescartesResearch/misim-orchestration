package cambio.simulator.orchestration.entities;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.resources.cpu.CPUProcess;
import desmoj.core.simulator.Model;

public class MicroserviceOrchestrationInstance extends MicroserviceInstance {
    public MicroserviceOrchestrationInstance(Model model, String name, boolean showInTrace, Microservice microservice,
                                int instanceID) {
        super(model, name, showInTrace, microservice, instanceID);
        if (!(model instanceof MiSimOrchestrationModel)) {
            throw new IllegalArgumentException("model must be of type MiSimOrchestrationModel");
        }
    }

    @Override
    protected void submitProcessToCPU(CPUProcess newProcess) {
        MiSimOrchestrationModel model = (MiSimOrchestrationModel) getModel();
        if (model.getOrchestrationConfig().isOrchestrate()){
            MicroserviceOrchestration owner = (MicroserviceOrchestration) this.getOwner();
            MicroserviceInstance nextAvailableInstance = owner.getNextAvailableInstance();
            nextAvailableInstance.getCpu().submitProcess(newProcess);
        } else {
            super.submitProcessToCPU(newProcess);
        }
    }
}
