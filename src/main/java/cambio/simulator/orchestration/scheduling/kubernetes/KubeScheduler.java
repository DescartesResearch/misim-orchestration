package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.export.Stats;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.parsing.kubernetes.KubernetesParser;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import com.google.gson.Gson;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1WatchEvent;
import lombok.Getter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KubeScheduler extends Scheduler {

    static String API_URL = "http://127.0.0.1:8000/";
    static String PATH_PODS = "updatePods";
    static String PATH_NODES = "updateNodes";

    // private static int counter = 1;

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
            String json = new Gson().toJson(nodeList);
            post(json, PATH_NODES);
        } catch (IOException e) {
            System.out.println("[INFO]: No connection to API server established. The kube scheduler is not supported in this run");
            //e.printStackTrace();
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
            V1PodList v1PodList = new V1PodList();
            v1PodList.setApiVersion("v1");
            v1PodList.setKind("PodList");
            List<V1WatchEvent> events = new ArrayList<>();
            List<Pod> allPodsPlacedOnNodes = ManagementPlane.getInstance().getAllPodsPlacedOnNodes();
            V1PodList podsToBePlaced = new V1PodList();
            boolean anyPodsHaveBeenRemoved = false;

            //Inform the scheduler that pods have been removed from nodes
            List<Pod> foundToRemove = new ArrayList<>();
            for (Pod pod : internalRunningPods) {
                //If MiSim does not hold the pod from the scheduler cache anymore, tell the scheduler that it was deleted
                if (!allPodsPlacedOnNodes.contains(pod)) {
                    anyPodsHaveBeenRemoved = true;
                    events.add(KubeObjectConverter.createPodDeletedEvent(pod, "Running"));
                    foundToRemove.add(pod);
                    System.out.println("In this iteration the following pod will be removed " + pod.getQuotedName() + " from node " + pod.getLastKnownNode().getQuotedName());
                }
            }
            foundToRemove.forEach(internalRunningPods::remove);

            //Inform the scheduler that pods have been removed from the scheduling queue
            foundToRemove.clear();
            for (Pod pod : internalPendingPods) {
                if (!podWaitingQueue.contains(pod)) {
                    events.add(KubeObjectConverter.createPodDeletedEvent(pod, "Pending"));
                    foundToRemove.add(pod);
                }
            }
            foundToRemove.forEach(internalPendingPods::remove);


            //Tell the scheduler that pods are already running on nodes (Scheduled by other schedulers)
            for (Pod pod : allPodsPlacedOnNodes) {
                v1PodList.addItemsItem(KubeObjectConverter.convertPod(pod, "Running"));
                if (!internalRunningPods.contains(pod)) {
                    events.add(KubeObjectConverter.createPodAddedEvent(pod, "Running"));
                    internalRunningPods.add(pod);
                }
            }

            //Add pods from the waiting queue
            while (podWaitingQueue.size() != 0) {
                Pod nextPodFromWaitingQueue = getNextPodFromWaitingQueue();
                if (!internalPendingPods.contains(nextPodFromWaitingQueue)) {
                    events.add(KubeObjectConverter.createPodAddedEvent(nextPodFromWaitingQueue, "Pending"));
                    internalPendingPods.add(nextPodFromWaitingQueue);
                    podsToBePlaced.addItemsItem(KubeObjectConverter.convertPod(nextPodFromWaitingQueue, "Pending"));
                } else if (anyPodsHaveBeenRemoved) {
                    // if pods have been removed, there is a chance that we get older pending pods placed
                    podsToBePlaced.addItemsItem(KubeObjectConverter.convertPod(nextPodFromWaitingQueue, "Pending"));
                }
                v1PodList.addItemsItem(KubeObjectConverter.convertPod(nextPodFromWaitingQueue, "Pending"));
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
            String json = new Gson().toJson(upr);
            String response = post(json, PATH_PODS);
            SchedulerResponse schedulerResponse = new Gson()
                    .fromJson(response, SchedulerResponse.class);
            // System.out.println(schedulerResponse);
            for (V1Node createdNode : schedulerResponse.getNewNodes()) {
                Node newNode = KubernetesParser.createNodeFromKubernetesObject(ManagementPlane.getInstance().getModel(), traceIsOn(), createdNode);
                ManagementPlane.getInstance().getCluster().addNode(newNode);
            }

            for (V1Node deletedNode : schedulerResponse.getDeletedNodes()) {
                Node delNode = KubernetesParser.createNodeFromKubernetesObject(ManagementPlane.getInstance().getModel(), traceIsOn(), deletedNode);
                ManagementPlane.getInstance().getCluster().deleteNode(delNode);
            }

            for (BindingInformation bind : schedulerResponse.getBinded()) {
                String boundNode = bind.getNode();
                String podName = bind.getPod();


                Node candidateNode = ManagementPlane.getInstance().getCluster().getNodeByName(boundNode);
                Pod pod = ManagementPlane.getInstance().getPodByName(podName);

                if (candidateNode == null) {
                    throw new KubeSchedulerException("The node that was selected by the kube-scheduler does not exist in the Simulation");
                } else if (pod == null) {
                    throw new KubeSchedulerException("The pod that was selected by the kube-scheduler does not exist in the Simulation");
                }

                if (!candidateNode.addPod(pod)) {
                    throw new KubeSchedulerException("The selected node has not enough resources to run the selected pod. The kube-scheduler must have calculated wrong");
                }

                internalRunningPods.add(pod);
                internalPendingPods.remove(pod);
                pod.bindToNode(boundNode);

                //only for reporting
                Stats.NodePodEventRecord record = new Stats.NodePodEventRecord();
                record.setTime((int) presentTime().getTimeAsDouble());
                record.setPodName(podName);
                record.setNodeName(boundNode);
                record.setScheduler("kube");
                record.setEvent("Binding");
                record.setOutcome("Success");
                record.setInfo("N/A");
                record.setDesiredState(pod.getOwner().getDesiredReplicaCount());
                record.setCurrentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner()));
                Stats.getInstance().getNodePodEventRecords().add(record);

                System.out.println(podName + " was bound on " + boundNode);

                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNode);
            }

            for (BindingFailureInformation fail : schedulerResponse.getFailed()) {
                String podName = fail.getPod();
                Pod pod = ManagementPlane.getInstance().getPodByName(podName);
                podWaitingQueue.add(pod);
                internalPendingPods.add(pod); // Should have no effect

                //only for reporting
                Stats.NodePodEventRecord record = new Stats.NodePodEventRecord();
                record.setTime((int) presentTime().getTimeAsDouble());
                record.setPodName(podName);
                record.setNodeName("N/A");
                record.setScheduler("kube");
                record.setEvent("Binding");
                record.setOutcome("Failed");
                record.setInfo(fail.getMessage());
                record.setDesiredState(pod.getOwner().getDesiredReplicaCount());
                record.setCurrentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(pod.getOwner()));
                Stats.getInstance().getNodePodEventRecords().add(record);

                System.out.println(this.getQuotedName() + " was not able to schedule pod " + pod + ". Reason: " + fail.getMessage());
                sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Reason: " + fail.getMessage());
                sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
            }
        } catch (IOException | KubeSchedulerException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String post(String content, String path) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        try (OutputStream os = con.getOutputStream()) {
            byte[] input = content.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }


    /*
    //    https://www.baeldung.com/httpurlconnection-post
    public JSONObject post(String content, int numberPendingPods, String deletedPods, String path) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonInputString = new JSONObject();
        jsonInputString.put("data", content);
        jsonInputString.put("numberPendingPods", numberPendingPods);
        jsonInputString.put("deletedPods", deletedPods);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return new JSONObject(response.toString());
        }

    }
    */
}
