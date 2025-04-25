package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.parsing.JsonTypeName;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName(value = "chaosmonkey_nodes", alternativeNames = {"chaos_monkey_nodes", "monkey_nodes"})
public class ChaosMonkeyForNodesEvent extends OrchestrationSelfScheduledExperimentAction {
    @Expose
    @SerializedName(value = "instances", alternate = {"instance_count", "killed_instance_count", "killed_instances"})
    private int instances;

    @Expose
    @SerializedName(value = "namePattern")
    private String namePattern;

    public ChaosMonkeyForNodesEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Instantiate a <code>ChaosMonkeyForPodsEvent</code>.
     *
     * @param owner          Model: The model that owns this event
     * @param name           String: The name of this event
     * @param showInTrace    boolean: Declaration if this event should be shown in the trace
     * @param namePattern    String: A regex: if node name matches regex it is considered as a candidate for failure
     * @param instances      int: The number of instances of the specified deployment you want to shut down, can be
     *                       greater than the number of currently running instances
     */
    public ChaosMonkeyForNodesEvent(Model owner, String name, boolean showInTrace, String namePattern,
                                   int instances, int retries) {
        super(owner, name, showInTrace);

        this.namePattern = namePattern;
        this.instances = instances;
        setSchedulingPriority(Priority.LOW);
    }

    /**
     * The eventRoutine of the <code>ChaosMonkeyForPodsEvent</code>. Terminates a specified number of instances of a
     * specified
     * <code>Deployment</code>.
     * Also tries to note the remaining number of instances in the trace.
     */
    @Override
    public void eventRoutine() throws SuspendExecution {
        List<Node> nodes = ManagementPlane.getInstance().getCluster().getNodes();
        Pattern pattern = Pattern.compile(namePattern);
        List<Node> candidates = nodes.stream().filter(n -> pattern.matcher(n.getPlainName()).find()).collect(Collectors.toList());
        List<Node> nodesToKill = new ArrayList<>(instances);
        if (candidates.size() < instances) {
            sendTraceNote("Could not execute ChaosMonkeyForNodesEvent because only " + candidates.size() + " candidates available, " + instances + " were required");
            return;
        } else if (candidates.size() == instances) {
            nodesToKill.addAll(candidates);
        } else {
            Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());
            for (int i = 0; i < instances; i++) {
                int randomIndex = random.nextInt(candidates.size());
                Node randomElement = candidates.get(randomIndex);
                candidates.remove(randomIndex);
                nodesToKill.add(randomElement);
            }
        }
        for (Node n : nodesToKill) {
            n.failInstantly();
            sendTraceNote("Chaos Monkey for Nodes was applied on " + n.getQuotedName());
        }

        for (Scheduler s : ManagementPlane.getInstance().getActiveSchedulers()) {
            s.onNodesRemoval(nodesToKill);
        }
    }

    @Override
    public String toString() {
        return "ChaosMonkeyForNodesEvent";
    }

}
