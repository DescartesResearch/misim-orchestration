package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.ContainerState;
import cambio.simulator.orchestration.entities.kubernetes.Deployment;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.entities.kubernetes.PodState;

import java.util.ArrayList;
import java.util.List;

public class ScalingUtils {
    public static double getAverageCPUUtilizationOfDeployment(Deployment deployment) {
        List<Double> podConsumptions = new ArrayList<>();
        for (Pod pod : deployment.getReplicaSet()) {
            if (pod.getPodState() == PodState.RUNNING) {
                double podCPUUtilization = 0;
                for (Container container : pod.getContainers()) {
                    if (container.getContainerState() == ContainerState.RUNNING) {
                        double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                        podCPUUtilization += relativeWorkDemand;
                    }
                }
                podConsumptions.add(podCPUUtilization);
            }
        }
        return podConsumptions.stream().mapToDouble(d -> d).average().orElse(0);
    }
}
