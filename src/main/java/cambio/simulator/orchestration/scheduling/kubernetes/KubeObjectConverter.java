package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.rest.dto.UpdateNodesRequest;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.util.*;

public class KubeObjectConverter {
    public static UpdateNodesRequest convertNodes(List<Node> clusterNodes) {
        V1NodeList nodeList = new V1NodeList();
        nodeList.setApiVersion("v1");
        nodeList.setKind("NodeList");
        List<V1Node> convertedNodes = new ArrayList<>();
        List<V1WatchEvent> events = new ArrayList<>();
        for (Node node : clusterNodes) {
            V1Node temp;
            if (node.getKubernetesRepresentation() == null) {
                temp = createNodeRepresentation(node);
                node.setKubernetesRepresentation(temp);
            } else {
                temp = node.getKubernetesRepresentation();
            }
            convertedNodes.add(temp);
            events.add(new V1WatchEvent().type("ADDED")._object(temp));
        }
        nodeList.setItems(convertedNodes);
        UpdateNodesRequest result = new UpdateNodesRequest();
        result.setAllNodes(nodeList);
        result.setEvents(events);
        result.setMachineSets(ManagementPlane.getInstance().getCluster().getMachineSets());
        result.setMachines(ManagementPlane.getInstance().getCluster().getMachines());
        return result;
    }

    public static UpdateNodesRequest deleteNodes(List<Node> clusterNodes, List<Node> deletedNodes) {
        V1NodeList nodeList = new V1NodeList();
        nodeList.setApiVersion("v1");
        nodeList.setKind("NodeList");
        List<V1Node> convertedNodes = new ArrayList<>();
        List<V1WatchEvent> events = new ArrayList<>();
        for (Node node : clusterNodes) {
            V1Node temp;
            if (node.getKubernetesRepresentation() == null) {
                temp = createNodeRepresentation(node);
                node.setKubernetesRepresentation(temp);
            } else {
                temp = node.getKubernetesRepresentation();
            }
            convertedNodes.add(temp);
        }
        nodeList.setItems(convertedNodes);
        for (Node node : deletedNodes) {
            V1Node temp;
            if (node.getKubernetesRepresentation() == null) {
                temp = createNodeRepresentation(node);
                node.setKubernetesRepresentation(temp);
            } else {
                temp = node.getKubernetesRepresentation();
            }
            V1NodeCondition condition = new V1NodeCondition().type("Ready").status("False");
            List<V1NodeCondition> conditionList = new ArrayList<>();
            conditionList.add(condition);
            if (temp.getStatus() == null) {
                temp.setStatus(new V1NodeStatus().conditions(conditionList));
            } else if (temp.getStatus().getConditions() == null) {
                temp.getStatus().setConditions(conditionList);
            } else {
                Optional<V1NodeCondition> condi = temp.getStatus().getConditions().stream().filter(c -> c.getType().equals("Ready")).findFirst();
                if (condi.isPresent()) {
                    condi.get().setStatus("False");
                } else {
                    temp.getStatus().getConditions().add(condition);
                }
            }
            V1Taint taint = new V1Taint().effect("NoSchedule").key("node.kubernetes.io/not-ready").value("True");
            List<V1Taint> taintList = new ArrayList<>();
            taintList.add(taint);
            if (temp.getSpec() == null) {
                temp.setSpec(new V1NodeSpec().taints(taintList));
            } else if (temp.getSpec().getTaints() == null) {
                temp.getSpec().setTaints(taintList);
            } else {
                Optional<V1Taint> tain = temp.getSpec().getTaints().stream().filter(t -> t.getKey().equals("node.kubernetes.io/not-ready")).findFirst();
                if (tain.isPresent()) {
                    tain.get().setEffect("NoSchedule");
                    tain.get().setValue("True");
                } else {
                    temp.getSpec().getTaints().add(taint);
                }
            }
            events.add(new V1WatchEvent().type("MODIFIED")._object(temp));
        }
        UpdateNodesRequest result = new UpdateNodesRequest();
        result.setAllNodes(nodeList);
        result.setEvents(events);
        result.setMachineSets(ManagementPlane.getInstance().getCluster().getMachineSets());
        result.setMachines(ManagementPlane.getInstance().getCluster().getMachines());
        return result;
    }

    private static V1Node createNodeRepresentation(Node node) {
        V1Node result = new V1Node();
        result.setApiVersion("v1");
        result.setKind("Node");
        result.setMetadata(new V1ObjectMeta().name(node.getPlainName()).labels(new HashMap<String, String>() {{
            put("kubernetes.io/hostname", node.getPlainName());
        }}));
        Map<String, Quantity> nodeResources = new HashMap<>();
        nodeResources.put("cpu", new Quantity(Double.toString(node.getTotalCPU())));
        // Some default arbitrary values for the non-modeled resources
        nodeResources.put("ephemeral-storage", new Quantity("999999999Ki"));
        nodeResources.put("hugepages-1Gi", new Quantity("0"));
        nodeResources.put("hugepages-2Mi", new Quantity("0"));
        nodeResources.put("memory", new Quantity("99999999Ki"));
        nodeResources.put("pods", new Quantity("110"));
        result.setStatus(new V1NodeStatus().allocatable(nodeResources).capacity(nodeResources));
        return result;
    }

    public static V1Pod convertPod(Pod pod, String status) {
        V1Pod result;
        if (pod.getKubernetesRepresentation() == null) {
            result = new V1Pod();
            result.setApiVersion("v1");
            result.setKind("Pod");
            result.setMetadata(new V1ObjectMeta().name(pod.getName()).namespace("default").uid(pod.getName()));
            V1PodSpec tempSpec = new V1PodSpec();
            List<V1Container> tempContainers = new ArrayList<>();
            for (Container c : pod.getContainers()) {
                V1Container tempContainer = new V1Container();
                tempContainer.setName(c.getPlainName());
                Map<String, Quantity> limitsAndRequests = new HashMap<>();
                limitsAndRequests.put("cpu", new Quantity(Double.toString(pod.getCPUDemand())));
                tempContainer.setResources(new V1ResourceRequirements().limits(limitsAndRequests).requests(limitsAndRequests));
                tempContainers.add(tempContainer);
            }
            tempSpec.setContainers(tempContainers);
            tempSpec.setSchedulerName("default-scheduler");
            // tempSpec.setSchedulerName(pod.getOwner().getSchedulerType().getDisplayName());
            if (status.equals("Running")) {
                tempSpec.setNodeName(pod.getLastKnownNode().getPlainName());
            }
            result.setSpec(tempSpec);
            pod.setKubernetesRepresentation(result);
        } else {
            result = pod.getKubernetesRepresentation();
        }
        result.setStatus(new V1PodStatus().phase(status));
        return result;
    }

    public static V1WatchEvent createPodAddedEvent(Pod pod, String status) {
        return new V1WatchEvent().type("ADDED")._object(convertPod(pod, status));
    }

    public static V1WatchEvent createPodDeletedEvent(Pod pod, String status) {
        return new V1WatchEvent().type("DELETED")._object(convertPod(pod, status));
    }
}
