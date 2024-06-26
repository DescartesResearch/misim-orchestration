package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.util.Util;
import cambio.simulator.parsing.JsonTypeName;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;

@JsonTypeName(value = "chaosmonkey_pods", alternativeNames = {"chaos_monkey_pods", "monkey_pods"})
public class ChaosMonkeyForPodsEvent extends OrchestrationSelfScheduledExperimentAction {
    @Expose
    @SerializedName(value = "instances", alternate = {"instance_count", "killed_instance_count", "killed_instances"})
    private int instances;

    @Expose
    @SerializedName(value = "deployment")
    private String deploymentName;

    public ChaosMonkeyForPodsEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Instantiate a <code>ChaosMonkeyForPodsEvent</code>.
     *
     * @param owner          Model: The model that owns this event
     * @param name           String: The name of this event
     * @param showInTrace    boolean: Declaration if this event should be shown in the trace
     * @param deploymentName String: The target deployment whose pod instances should be terminated
     * @param instances      int: The number of instances of the specified deployment you want to shut down, can be
     *                       greater than the number of currently running instances
     */
    public ChaosMonkeyForPodsEvent(Model owner, String name, boolean showInTrace, String deploymentName,
                                   int instances, int retries) {
        super(owner, name, showInTrace);

        this.deploymentName = deploymentName;
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
        final Deployment deployment = Util.findDeploymentByName(deploymentName);
        if (deployment != null) {
            deployment.killPodInstances(instances, 0, null);

            sendTraceNote("Chaos Monkey for Pods was applied on " + deployment.getQuotedName());
            boolean hasServicesLeft = deployment.getCurrentRunningOrPendingReplicaCount() > 0;
            sendTraceNote(String.format("There are %s pods left of deployment %s",
                    hasServicesLeft ? String.format("still %d", deployment.getCurrentRunningOrPendingReplicaCount())
                            : "no",
                    deployment.getName()));
        } else {
            sendTraceNote("Could not execute ChaosMonkeyForPodsEvent because the deployment from the " +
                    "given experiment file with the name '" + deploymentName + "' is unknown");
        }
    }

    @Override
    public String toString() {
        return "ChaosMonkeyForPodsEvent";
    }

}
