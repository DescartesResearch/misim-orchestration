package cambio.simulator.orchestration.events;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.NodeList;
import cambio.simulator.orchestration.export.Stats;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.Scheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class NodeNoExecuteTaintEvent extends Event<NodeList> {

    public NodeNoExecuteTaintEvent(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine(NodeList nodeList) throws SuspendExecution {
        for (Scheduler s : ManagementPlane.getInstance().getActiveSchedulers()) {
            s.onNodeNoExecuteTaint(nodeList.getNodes());
        }

        for (Node n : nodeList.getNodes()) {
            Stats.getInstance().createNodeStatusStats((int) presentTime().getTimeAsDouble(),
                    n.getQuotedName(), "NoExecuteTaint");
        }

    }

    @Override
    public String toString() {
        return "NodeUnreachableEvent";
    }
}
