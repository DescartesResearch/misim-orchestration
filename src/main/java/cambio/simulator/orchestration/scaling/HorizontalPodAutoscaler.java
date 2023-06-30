package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.entities.kubernetes.Deployment;

public class HorizontalPodAutoscaler extends AutoScaler {

    // TODO Maybe also include via adapter, upscaling/downscaling behavior not 100% as in Kubernetes, e.g. see HorizontalPodAutoscalerBehavior
    // https://github.com/kubernetes/kubernetes/blob/master/pkg/apis/autoscaling/types.go#L113

    private final double targetUtilization;
    private final int minReplicas;
    private final int maxReplicas;

    public HorizontalPodAutoscaler(double targetUtilization, int minReplicas, int maxReplicas) {
        super();
        this.rename("HPA");
        this.targetUtilization = targetUtilization;
        this.minReplicas = minReplicas;
        this.maxReplicas = maxReplicas;
    }

    @Override
    public void apply(Deployment deployment) {
        //https://github.com/kubernetes/kubernetes/blob/8caeec429ee1d2a9df7b7a41b21c626346b456fb/docs/design/horizontal-pod-autoscaler.md#autoscaling-algorithm
//        Scale-up can only happen if there was no rescaling within the last 3 minutes. Scale-down will wait for 5 minutes from the last rescaling.
//        Moreover any scaling will only be made if: avg(CurrentPodsConsumption) / Target drops below 0.9 or increases above 1.1 (10% tolerance)

        double avg2Target = ScalingUtils.getAverageCPUUtilizationOfDeployment(deployment) / targetUtilization;
        // Tolerance area
        if (avg2Target > 0.9 && avg2Target < 1.1) {
            sendTraceNote("No Scaling required for " + deployment + ".");
            return;
        }

        int desiredReplicas = (int) Math.ceil(avg2Target * deployment.getCurrentRunningOrPendingReplicaCount());

        desiredReplicas = Math.min(desiredReplicas, maxReplicas);
        desiredReplicas = Math.max(minReplicas, desiredReplicas);


        if (desiredReplicas != deployment.getCurrentRunningOrPendingReplicaCount()) {
            if (desiredReplicas > deployment.getCurrentRunningOrPendingReplicaCount()) {
                deployment.setDesiredReplicaCount(desiredReplicas);
                sendTraceNote("Scaling Up " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);

            } else {
                deployment.setDesiredReplicaCount(desiredReplicas);
                sendTraceNote("Scaling Down " + deployment + ". From " + deployment.getCurrentRunningOrPendingReplicaCount() + " -> " + desiredReplicas);
            }
        } else {
            sendTraceNote("No Scaling required for " + deployment + ".");
        }
    }
}
