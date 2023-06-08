package cambio.simulator.orchestration.entities;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.models.OrchestrationConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Cluster {
    private static final Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());
    private List<Node> nodes;
    private Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> delayMap;

    public Cluster(List<Node> nodes, Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> network) {
        this.nodes = nodes;
        this.delayMap = network;
    }

    public Node getNodeByName(String name){
        Optional<Node> first = nodes.stream().filter(node -> node.getPlainName().equals(name)).findFirst();
        return first.orElse(null);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public double getNetworkDelay(String sourceNode, String targetNode) {
        if (delayMap == null) return 0;
        OrchestrationConfig.NetworkDelays.NetworkInfo networkInfo = null;
        if (delayMap.containsKey(sourceNode)) {
            if (delayMap.get(sourceNode).containsKey(targetNode)) {
                networkInfo = delayMap.get(sourceNode).get(targetNode);
            }
        } else if (delayMap.containsKey(targetNode)) {
            if (delayMap.get(targetNode).containsKey(sourceNode)) {
                networkInfo = delayMap.get(targetNode).get(sourceNode);
            }
        }
        if (networkInfo == null) return 0;
        else return random.nextGaussian() * networkInfo.getStd() + networkInfo.getMean();
    }
}
