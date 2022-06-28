package cambio.simulator.orchestration.events;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.entities.MicroserviceOrchestration;
import cambio.simulator.orchestration.entities.Container;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class RestartStartContainerAndMicroserviceInstanceEvent extends Event<Container> {
    public static int counter = 0;
    public RestartStartContainerAndMicroserviceInstanceEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine(Container container) {
        MicroserviceInstance currentMicroserviceInstance = container.getMicroserviceInstance();

        MicroserviceOrchestration owner = (MicroserviceOrchestration) currentMicroserviceInstance.getOwner();
        MicroserviceInstance newMicroServiceInstance = owner.createMicroServiceInstance();
        owner.getInstancesSet().add(newMicroServiceInstance);
        container.setMicroserviceInstance(newMicroServiceInstance);
        container.start();


        //Would be nice to restart the real container...but this produces negative values for the demandremainder
//        //state must be switched from KILLED to SHUTDOWN. Otherwise start method would throw error
//        currentMicroserviceInstance.setState(InstanceState.SHUTDOWN);
//        currentMicroserviceInstance.activatePatterns(owner.getInstanceOwnedPatternConfigurations());
//        container.start();
//        //Needs to be added again to MicroServiceInstances. Otherwise, a following chaos monkey event would not find an instance to kill
//        currentMicroserviceInstance.getOwner().getInstancesSet().add(currentMicroserviceInstance);
//        sendTraceNote(currentMicroserviceInstance.getQuotedName() + " was restarted");

    }
}
