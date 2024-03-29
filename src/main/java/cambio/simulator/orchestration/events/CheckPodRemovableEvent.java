package cambio.simulator.orchestration.events;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

public class CheckPodRemovableEvent  extends EventOf2Entities<Pod, Node> {

    public CheckPodRemovableEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
//        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Pod pod, Node node) {
        ManagementPlane.getInstance().checkIfPodRemovableFromNode(pod, node);
    }


}
