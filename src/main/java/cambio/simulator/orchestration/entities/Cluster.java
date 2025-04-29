package cambio.simulator.orchestration.entities;

import cambio.simulator.orchestration.entities.kubernetes.Node;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.models.OrchestrationConfig;
import cambio.simulator.orchestration.parsing.kubernetes.KubernetesObjectWithMetadataSpec;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

public class Cluster {
    private static final Random random = new Random(ManagementPlane.getInstance().getExperimentSeed());

    @Getter
    @Setter
    private List<Node> nodes;

    @Getter
    @Setter
    private List<KubernetesObjectWithMetadataSpec> machineSets;

    @Getter
    @Setter
    private List<KubernetesObjectWithMetadataSpec> machines;
    private Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> delayMap;
    private List<OrchestrationConfig.StartUpTimeNode> nodeStartTimes;

    public Cluster(List<Node> nodes, Map<String, Map<String, OrchestrationConfig.NetworkDelays.NetworkInfo>> network, List<OrchestrationConfig.StartUpTimeNode> nodeStartTimes) {
        this.nodes = nodes;
        this.delayMap = network;
        this.nodeStartTimes = nodeStartTimes;
    }

    public Node getNodeByName(String name) {
        Optional<Node> first = nodes.stream().filter(node -> node.getPlainName().equals(name)).findFirst();
        return first.orElse(null);
    }

    public void addNode(Node node) {
        if (node.getStartTime() == -1 && nodeStartTimes != null) {
            Optional<OrchestrationConfig.StartUpTimeNode> startTime = nodeStartTimes.stream().filter(x -> Pattern.compile(x.getPattern()).matcher(node.getPlainName()).find()).findFirst();
            if (startTime.isPresent()) {
                node.setStartTime(startTime.get().getTime());
            } else {
                node.setStartTime(0);
            }
        } else {
            node.setStartTime(0);
        }
        nodes.add(node);
    }

    public void deleteNode(Node node) {
        nodes.remove(node);
    }

    public double getNetworkDelay(String sourceNode, String targetNode) {
        if (delayMap == null) return 0;
        OrchestrationConfig.NetworkDelays.NetworkInfo networkInfo = null;
        // Search for exact match
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
        // Search for pattern
        Optional<String> sourceMatch = delayMap.keySet().stream().filter(s -> Pattern.compile(s).matcher(sourceNode).find()).findFirst();
        if (sourceMatch.isPresent()) {
            Optional<String> targetMatch = delayMap.get(sourceMatch.get()).keySet().stream().filter(s -> Pattern.compile(s).matcher(targetNode).find()).findFirst();
            if (targetMatch.isPresent()) {
                networkInfo = delayMap.get(sourceMatch.get()).get(targetMatch.get());
            }
        }
        if (networkInfo == null) {
            Optional<String> targetMatch = delayMap.keySet().stream().filter(s -> Pattern.compile(s).matcher(targetNode).find()).findFirst();
            if (targetMatch.isPresent()) {
                sourceMatch = delayMap.get(targetMatch.get()).keySet().stream().filter(s -> Pattern.compile(s).matcher(sourceNode).find()).findFirst();
                if (sourceMatch.isPresent()) {
                    networkInfo = delayMap.get(targetMatch.get()).get(sourceMatch.get());
                }
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
