package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
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
                temp = new V1Node();
                temp.setApiVersion("v1");
                temp.setKind("Node");
                temp.setMetadata(new V1ObjectMeta().name(node.getPlainName()).labels(new HashMap<String, String>() {{
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
                temp.setStatus(new V1NodeStatus().allocatable(nodeResources).capacity(nodeResources));
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
