package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.export.Stats;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.kubernetes.KubernetesParser;
import cambio.simulator.orchestration.rest.KubeSchedulerController;
import cambio.simulator.orchestration.rest.dto.*;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1WatchEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KubeScheduler extends Scheduler {

    // mirrors the internal cache of running pods that are known by the scheduler
    Set<Pod> internalRunningPods = new HashSet<>();
    Set<Pod> internalPendingPods = new HashSet<>();
    static int COUNTER = 1;

    @Getter
    private static final KubeScheduler instance = new KubeScheduler();

    //private constructor to avoid client applications to use constructor
    private KubeScheduler() {
        this.rename("KubeScheduler");

        try {
            UpdateNodesRequest nodeList = KubeObjectConverter.convertNodes(cluster.getNodes());
            KubeSchedulerController.updateNodes(nodeList);
        } catch (IOException e) {
            System.out.println("[INFO]: No connection to API server established. The kube scheduler is not supported "
                    + "in this run");
        }
    }

    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.KUBE;
    }


    @Override
    public void schedulePods() {
        try {
            if (podWaitingQueue.isEmpty()) return;
            List<Pod> allPodsPlacedOnNodes = ManagementPlane.getInstance().getAllPodsPlacedOnNodes();
            List<V1WatchEvent> events = removeInternalRunningPods(allPodsPlacedOnNodes);
            boolean removedRunningPods = !events.isEmpty();

            List<V1WatchEvent> pendingPodsEvents = removePendingPods();
            events.addAll(pendingPodsEvents);

            //Tell the scheduler that pods are already running on nodes (Scheduled by other schedulers)
            V1PodList v1PodList = new V1PodList();
            v1PodList.setApiVersion("v1");
            v1PodList.setKind("PodList");
            for (Pod pod : allPodsPlacedOnNodes) {
                v1PodList.addItemsItem(KubeObjectConverter.convertPod(pod, "Running"));
                if (!internalRunningPods.contains(pod)) {
                    events.add(KubeObjectConverter.createPodAddedEvent(pod, "Running"));
                    internalRunningPods.add(pod);
                }
            }

            //Add pods from the waiting queue
            V1PodList podsToBePlaced = new V1PodList();
            while (!podWaitingQueue.isEmpty()) {
                Pod nextPod = getNextPodFromWaitingQueue();
                if (!internalPendingPods.contains(nextPod)) {
                    events.add(KubeObjectConverter.createPodAddedEvent(nextPod, "Pending"));
                    internalPendingPods.add(nextPod);
                    podsToBePlaced.addItemsItem(KubeObjectConverter.convertPod(nextPod, "Pending"));
                } else if (removedRunningPods) {
                    // if pods have been removed, there is a chance that we get older pending pods placed
                    podsToBePlaced.addItemsItem(KubeObjectConverter.convertPod(nextPod, "Pending"));
                }
                v1PodList.addItemsItem(KubeObjectConverter.convertPod(nextPod, "Pending"));
            }

            if (events.isEmpty()) {
                // If no events happened, scheduler will do nothing
                podWaitingQueue.addAll(internalPendingPods);
                return;
            }

            UpdatePodsRequest upr = new UpdatePodsRequest();
            upr.setEvents(events);
            upr.setAllPods(v1PodList);
            upr.setPodsToBePlaced(podsToBePlaced);

            System.out.println("Call kube-scheduler: " + COUNTER++);
            SchedulerResponse schedulerResponse = KubeSchedulerController.updatePods(upr);
            handleSchedulerResponse(schedulerResponse);
        } catch (IOException | KubeSchedulerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void handleSchedulerResponse(SchedulerResponse schedulerResponse) throws KubeSchedulerException {
        Model model = ManagementPlane.getInstance().getModel();
        for (V1Node createdNode : schedulerResponse.getNewNodes()) {
            Node newNode = KubernetesParser.createNodeFromKubernetesObject(model, traceIsOn(), createdNode);
            ManagementPlane.getInstance().getCluster().addNode(newNode);
        }

        for (V1Node deletedNode : schedulerResponse.getDeletedNodes()) {
            Node delNode = KubernetesParser.createNodeFromKubernetesObject(model, traceIsOn(), deletedNode);
            ManagementPlane.getInstance().getCluster().deleteNode(delNode);
        }

        for (BindingInformation bind : schedulerResponse.getBinded()) {
            String boundNode = bind.getNode();
            String podName = bind.getPod();
            Node candidateNode = ManagementPlane.getInstance().getCluster().getNodeByName(boundNode);
            Pod pod = ManagementPlane.getInstance().getPodByName(podName);

            if (candidateNode == null) throw new KubeSchedulerException.NodeDoesNotExist();
            else if (pod == null) throw new KubeSchedulerException.PodDoesNotExist();
            else if (!candidateNode.addPod(pod)) throw new KubeSchedulerException.NodeFull();

            internalRunningPods.add(pod);
            internalPendingPods.remove(pod);
            pod.bindToNode(boundNode);

            int desiredState = pod.getOwner().getDesiredReplicaCount();
            int currentState = ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner());
            int time = (int) presentTime().getTimeAsDouble();
            Stats.NodePodEventRecord record =
                    Stats.NodePodEventRecord.builder().fromBindingInformation(bind).time(time).desiredState(desiredState).currentState(currentState).build();
            Stats.getInstance().getNodePodEventRecords().add(record);
            System.out.println(podName + " was bound on " + boundNode);
            sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNode);
        }

        for (BindingFailureInformation fail : schedulerResponse.getFailed()) {
            Pod pod = ManagementPlane.getInstance().getPodByName(fail.getPod());
            podWaitingQueue.add(pod);
            internalPendingPods.add(pod); // Should have no effect
            int desiredState = pod.getOwner().getDesiredReplicaCount();
            int currentState = ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner());
            int time = (int) presentTime().getTimeAsDouble();
            Stats.NodePodEventRecord record =
                    Stats.NodePodEventRecord.builder().fromBindingFailureInformation(fail).time(time).desiredState(desiredState).currentState(currentState).build();
            Stats.getInstance().getNodePodEventRecords().add(record);
            System.out.println(this.getQuotedName() + " was not able to schedule pod " + pod + ". Reason: " + fail.getMessage());
            sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Reason: " + fail.getMessage());
            sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
        }
    }

    @NotNull
    private List<V1WatchEvent> removePendingPods() {
        List<V1WatchEvent> pendingPodsEvents = new ArrayList<>();
        //Inform the scheduler that pods have been removed from the scheduling queue
        List<Pod> internalPendingPodsToRemove = new ArrayList<>();
        for (Pod pod : internalPendingPods) {
            if (!podWaitingQueue.contains(pod)) {
                pendingPodsEvents.add(KubeObjectConverter.createPodDeletedEvent(pod, "Pending"));
                internalPendingPodsToRemove.add(pod);
            }
        }
        internalPendingPodsToRemove.forEach(internalPendingPods::remove);
        return pendingPodsEvents;
    }

    private List<V1WatchEvent> removeInternalRunningPods(List<Pod> allPodsPlacedOnNodes) {
        List<Pod> internalRunningPodsToRemove = new ArrayList<>();
        List<V1WatchEvent> events = new ArrayList<>(); // gather events for informing the scheduler that pods
        // have been removed from nodes
        for (Pod pod : internalRunningPods) {
            //If MiSim does not hold the pod from the scheduler cache anymore, tell the scheduler that it was deleted
            if (!allPodsPlacedOnNodes.contains(pod)) {
                events.add(KubeObjectConverter.createPodDeletedEvent(pod, "Running"));
                internalRunningPodsToRemove.add(pod);
                System.out.println("In this iteration the following pod will be removed " + pod.getQuotedName() + " " + "from node " + pod.getLastKnownNode().getQuotedName());
            }
        }
        internalRunningPodsToRemove.forEach(internalRunningPods::remove);
        return events;
    }


}
