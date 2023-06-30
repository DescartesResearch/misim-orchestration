package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import desmoj.core.simulator.TimeInstant;

// SimpleReactiveAutoscaler is the implementation of MiSim's ReactiveAutoscalingPolicy
public class SimpleReactiveAutoscaler extends AutoScaler {

    private final double lowerBound;
    private final double upperBound;
    private TimeInstant lastScale;
    private final int holdTime;

    public SimpleReactiveAutoscaler(double lowerBound, double upperBound, int holdTime) {
        super();
        this.rename("SimpleReactiveAutoscaler");
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.holdTime = holdTime;
        lastScale = new TimeInstant(Double.MIN_VALUE);
    }

    @Override
    public void apply(Deployment deployment) {

        TimeInstant presentTime = deployment.presentTime();
        int currentInstanceCount = deployment.getCurrentRunningOrPendingReplicaCount();
        double avg = ScalingUtils.getAverageCPUUtilizationOfDeployment(deployment);

        if (currentInstanceCount <= 0) { //starts a instances if there are none
            deployment.setDesiredReplicaCount(1);
        } else if (avg >= upperBound) {
            double upScalingFactor = avg / (upperBound - 0.01);
            int newInstanceCount = (int) Math.max(1, Math.ceil(currentInstanceCount * upScalingFactor));
            deployment.setDesiredReplicaCount(newInstanceCount);
            lastScale = presentTime;
        } else if (avg <= lowerBound
                && currentInstanceCount > 1
                && presentTime.getTimeAsDouble() - lastScale.getTimeAsDouble() > holdTime) {
            double downScaleFactor = Math.max(0.01, avg) / lowerBound;
            int newInstanceCount = (int) Math.max(1, Math.ceil(currentInstanceCount * downScaleFactor));
            deployment.setDesiredReplicaCount(newInstanceCount);
            lastScale = presentTime;
        }
        if (deployment.getDesiredReplicaCount() != currentInstanceCount) {
            sendTraceNote(String.format("Changed target instance count of deployment %s to %d", deployment.getPlainName(), deployment.getDesiredReplicaCount()));
        } else {
            sendTraceNote(String.format("No scaling needed for deployment %s", deployment.getPlainName()));
        }
    }
}
