package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.entities.kubernetes.Deployment;

public class FakeAutoscaler extends AutoScaler {

    private final int maxReplicas;
    private final int increment;
    private final int decrement;

    public FakeAutoscaler(int upperReplicaLimit, int increment, int decrement) {
        super();
        this.rename("FakeAutoscaler");
        this.maxReplicas = upperReplicaLimit;
        this.increment = increment;
        this.decrement = decrement;
    }
    @Override
    public void apply(Deployment deployment) {
        if (deployment.getDesiredReplicaCount() < maxReplicas) {
            deployment.setDesiredReplicaCount(deployment.getDesiredReplicaCount() + increment);
        } else {
            deployment.setDesiredReplicaCount(deployment.getDesiredReplicaCount() - decrement);
        }
    }
}
