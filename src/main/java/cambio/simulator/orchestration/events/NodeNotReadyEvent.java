package cambio.simulator.orchestration.events;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.NodeList;
import cambio.simulator.orchestration.entities.kubernetes.PodState;
import cambio.simulator.orchestration.export.Stats;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.Scheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class NodeNotReadyEvent extends Event<NodeList> {

    public NodeNotReadyEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine(NodeList nodeList) throws SuspendExecution {
        for (Scheduler s : ManagementPlane.getInstance().getActiveSchedulers()) {
            s.onNodeNotReady(nodeList.getNodes());
        }

        for (Node n : nodeList.getNodes()) {
            n.getPods().forEach(pod -> pod.transitionToState(PodState.UNKNOWN));
            Stats.getInstance().createNodeStatusStats((int) presentTime().getTimeAsDouble(),
                    n.getQuotedName(), "NotReady");
        }

    }

    @Override
    public String toString() {
        return "NodeNotReadyEvent";
    }
}
