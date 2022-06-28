package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class StartContainerAndMicroserviceInstanceEvent extends Event<Container> {
    public static int counter = 0;
    public StartContainerAndMicroserviceInstanceEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine(Container container) {
        container.getMicroserviceInstance().start();
        container.setContainerState(ContainerState.RUNNING);
    }
}
