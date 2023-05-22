package cambio.simulator.orchestration.scheduling.kubernetes;

import cambio.simulator.orchestration.entities.Node;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1NodeStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KubeObjectConverter {
    public static V1NodeList convertNodes(List<Node> clusterNodes) {
        V1NodeList result = new V1NodeList();
        List<V1Node> convertedNodes = new ArrayList<>();
        for (Node node : clusterNodes) {
            V1Node temp = new V1Node();
            temp.setMetadata(new V1ObjectMeta().name(node.getPlainName()));
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
        }
        return result.items(convertedNodes);
    }
}
