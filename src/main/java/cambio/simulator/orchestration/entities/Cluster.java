package cambio.simulator.orchestration.entities;

import java.util.List;
import java.util.Optional;

public class Cluster {
    private List<Node> nodes;

    public Cluster(List<Node> nodes) {
        this.nodes = nodes;
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

}
