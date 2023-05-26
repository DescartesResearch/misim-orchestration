package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.entities.Container;
import cambio.simulator.orchestration.entities.Node;
import cambio.simulator.orchestration.entities.kubernetes.Affinity;
import cambio.simulator.orchestration.entities.kubernetes.Pod;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;

import java.util.*;

public class KubeObjectConverter {
    public static UpdateNodesRequest convertNodes(List<Node> clusterNodes) {
        V1NodeList nodeList = new V1NodeList();
        List<V1Node> convertedNodes = new ArrayList<>();
        List<V1WatchEvent> events = new ArrayList<>();
        for (Node node : clusterNodes) {
            V1Node temp = new V1Node();
            temp.setApiVersion("v1");
            temp.setKind("Node");
            temp.setMetadata(new V1ObjectMeta().name(node.getPlainName()).labels(new HashMap<String, String>() {{
                put("kubernetes.io/hostname", node.getPlainName());
            }}));
            Map<String, Quantity> nodeResources = new HashMap<>();
            nodeResources.put("cpu", new Quantity(Integer.toString(node.getTotalCPU())));
            // Some default arbitrary values for the non-modeled resources
            nodeResources.put("ephemeral-storage", new Quantity("999999999Ki"));
            nodeResources.put("hugepages-1Gi", new Quantity("0"));
            nodeResources.put("hugepages-2Mi", new Quantity("0"));
            nodeResources.put("memory", new Quantity("99999999Ki"));
            nodeResources.put("pods", new Quantity("110"));
            temp.setStatus(new V1NodeStatus().allocatable(nodeResources).capacity(nodeResources));
            convertedNodes.add(temp);
            events.add(new V1WatchEvent().type("ADDED")._object(temp));
        }
        nodeList.setItems(convertedNodes);
        UpdateNodesRequest result = new UpdateNodesRequest();
        result.setAllNodes(nodeList);
        result.setEvents(events);
        return result;
    }

    public static V1Pod convertPod(Pod pod, String status) {
        V1Pod result = new V1Pod();
        result.setApiVersion("v1");
        result.setKind("Pod");
        result.setMetadata(new V1ObjectMeta().name(pod.getName()).namespace("default").uid(pod.getName()));
        V1PodSpec tempSpec = new V1PodSpec();
        if (!pod.getOwner().getAffinity().getNodeAffinities().isEmpty()) {
            tempSpec.setAffinity(convertAffinity(pod.getOwner().getAffinity()));
        }
        List<V1Container> tempContainers = new ArrayList<>();
        for (Container c : pod.getContainers()) {
            V1Container tempContainer = new V1Container();
            tempContainer.setName(c.getPlainName());
            Map<String, Quantity> limitsAndRequests = new HashMap<>();
            limitsAndRequests.put("cpu", new Quantity(Integer.toString(pod.getCPUDemand())));
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
        result.setStatus(new V1PodStatus().phase(status));
        return result;
    }

    public static V1Affinity convertAffinity(Affinity affinity) {
        V1Affinity result = new V1Affinity();
        result.setNodeAffinity(new V1NodeAffinity().requiredDuringSchedulingIgnoredDuringExecution(
                new V1NodeSelector().addNodeSelectorTermsItem(
                        new V1NodeSelectorTerm().addMatchExpressionsItem(
                                new V1NodeSelectorRequirement()
                                        .key("kubernetes.io/hostname")
                                        .operator("In")
                                        .values(new ArrayList<>(affinity.getNodeAffinities()))))));
        return result;
    }

    public static V1WatchEvent createPodDeletedEvent(Pod pod) {
        return new V1WatchEvent().type("DELETED")._object(convertPod(pod, "Failed"));
    }

    public static V1WatchEvent createPodAddedEvent(Pod pod, String status) {
        return new V1WatchEvent().type("ADDED")._object(convertPod(pod, status));
    }
}
