package cambio.simulator.orchestration.scheduling.kubernetes;

public class KubeSchedulerException extends Exception {
    public KubeSchedulerException(String errorMessage) {
        super(errorMessage);
    }

    public static class NodeDoesNotExist extends KubeSchedulerException {
        public NodeDoesNotExist() {
            super("The node that was selected by the kube-scheduler does not exist in the Simulation.");
        }
    }

    public static class PodDoesNotExist extends KubeSchedulerException {
        public PodDoesNotExist() {
            super("The pod that was selected by the kube-scheduler does not exist in the Simulation");
        }
    }

    public static class NodeFull extends KubeSchedulerException {
        public NodeFull() {
            super("The selected node has not enough resources to run the selected pod. The kube-scheduler must have " + "calculated wrong");
        }
    }
}