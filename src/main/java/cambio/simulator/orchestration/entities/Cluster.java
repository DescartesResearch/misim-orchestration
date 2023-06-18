package cambio.simulator.orchestration.entities;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.orchestration.parsing.kubernetes.KubernetesObjectWithMetadataSpec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Cluster {
    private static final Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());
    private List<Node> nodes;
    private List<KubernetesObjectWithMetadataSpec> machineSets;
    private List<KubernetesObjectWithMetadataSpec> machines;
    private Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> delayMap;

    public Cluster(List<Node> nodes, Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> network) {
        this.nodes = nodes;
        this.delayMap = network;
    }

    public Node getNodeByName(String name){
        Optional<Node> first = nodes.stream().filter(node -> node.getPlainName().equals(name)).findFirst();
        return first.orElse(null);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void deleteNode(Node node) {
        nodes.remove(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setMachineSets(List<KubernetesObjectWithMetadataSpec> machineSets) {
        this.machineSets = machineSets;
    }

    public List<KubernetesObjectWithMetadataSpec> getMachineSets() {
        return machineSets;
    }

    public List<KubernetesObjectWithMetadataSpec> getMachines() {
        return machines;
    }

    public void setMachines(List<KubernetesObjectWithMetadataSpec> machines) {
        this.machines = machines;
    }

    public double getNetworkDelay(String sourceNode, String targetNode) {
        if (delayMap == null) return 0;
        OrchestrationConfig.NetworkDelays.NetworkInfo networkInfo = null;
        if (delayMap.containsKey(sourceNode)) {
            if (delayMap.get(sourceNode).containsKey(targetNode)) {
                networkInfo = delayMap.get(sourceNode).get(targetNode);
            }
        }
        if (networkInfo == null && delayMap.containsKey(targetNode)) {
            if (delayMap.get(targetNode).containsKey(sourceNode)) {
                networkInfo = delayMap.get(targetNode).get(sourceNode);
            }
        }
        if (networkInfo == null) {
            // System.out.printf("[DEBUG] No network info found for source node %s and target node %s\n", sourceNode, targetNode);
            return 0;
        } else {
            double delay = random.nextGaussian() * networkInfo.getStd() + networkInfo.getMean();
            // System.out.printf("[DEBUG] Adding delay %f between node %s and node %s\n", delay, sourceNode, targetNode);
            return delay;
        }
    }
}
