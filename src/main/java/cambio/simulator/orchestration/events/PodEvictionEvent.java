package cambio.simulator.orchestration.events;

import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.entities.kubernetes.PodList;
import cambio.simulator.orchestration.entities.kubernetes.PodState;
import cambio.simulator.orchestration.management.ManagementPlane;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class PodEvictionEvent extends Event<PodList> {

    public PodEvictionEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine(PodList podList) throws SuspendExecution {
        for (String name : podList.getPods()) {
            Pod pod = ManagementPlane.getInstance().getPodByName(name);
            for (Container container : pod.getContainers()) {
                // Set to null so die() does not try to remove the instance again.
                container.setMicroserviceInstance(null);
            }
            pod.transitionToState(PodState.FAILED);
        }
        // Add as exporter if needed
        StringBuilder builder = new StringBuilder();
        for (String name : podList.getPods()) {
            builder.append("Pod '");
            builder.append(name);
            builder.append("' was evicted.\n");
        }
        System.out.println(builder.toString());
    }

    @Override
    public String toString() {
        return "PodEvictionEvent";
    }
}
