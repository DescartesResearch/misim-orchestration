package cambio.simulator.orchestration.scheduling;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.*;

public class RandomScheduler extends Scheduler{

    Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());

    private static final RandomScheduler instance = new RandomScheduler();

    //private constructor to avoid client applications to use constructor
    private RandomScheduler() {
        this.rename("RandomScheduler");

    }

    public static RandomScheduler getInstance() {
        return instance;
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

        if (pod != null) {
            Node candidateNote = null;
            double cpuDemand = pod.getCPUDemand();
            List<Node> nodes = new ArrayList<>(cluster.getNodes());
            Collections.shuffle(nodes, random);
            for (Node node : nodes) {
                if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                    candidateNote = node;
                    break;
                }
            }
            if (candidateNote != null) {
                candidateNote.addPod(pod);
                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNote);
                return true;
            } else {
                podWaitingQueue.add(pod);
                sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Unsufficient resources!");
                sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
                return false;
            }
        }
        sendTraceNote(this.getQuotedName() + " has no pods left for scheduling");
        return false;
    }

    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.RANDOM;
    }

}
