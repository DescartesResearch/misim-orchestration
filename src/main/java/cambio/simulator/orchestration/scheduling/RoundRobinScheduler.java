package cambio.simulator.orchestration.scheduling;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import lombok.Getter;

public class RoundRobinScheduler extends Scheduler {
    @Getter
    private static final RoundRobinScheduler instance = new RoundRobinScheduler();

    //private constructor to avoid client applications to use constructor
    private RoundRobinScheduler() {
        this.rename("RoundRobinScheduler");
    }

    @Override
    public void schedulePods() {
        if (podWaitingQueue.isEmpty()) {
            getModel().sendTraceNote(this.getQuotedName() + " 's Waiting Queue is empty.");
            return;
        }
        int i = 0;
        final int podWaitingQueueInitSize = podWaitingQueue.size();
        while (i < podWaitingQueueInitSize) {
            i++;
            schedulePod();
        }
    }

    public boolean schedulePod() {

        final Pod pod = getNextPodFromWaitingQueue();
        String plainName = pod.getOwner().getPlainName();

        if (pod != null) {
            Node candidateNode = null;
            int candidateNodeSize = Integer.MAX_VALUE;
            double cpuDemand = pod.getCPUDemand();
            for (Node node : cluster.getNodes()) {
                int size = (int) node.getPods().stream().filter(pod1 -> pod1.getOwner().getPlainName().equals(plainName)).count();
                if (size < candidateNodeSize) {
                    if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                        candidateNodeSize = size;
                        candidateNode = node;
                    }
                }

            }
            if (candidateNode != null) {
                candidateNode.addPod(pod);
                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNode);
                return true;
            } else {
                podWaitingQueue.add(pod);
                sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Insufficient resources!");
                sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
                return false;
            }
        }
        sendTraceNote(this.getQuotedName() + " has no pods left for scheduling");
        return false;
    }

    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.ROUNDROBIN;
    }


}
