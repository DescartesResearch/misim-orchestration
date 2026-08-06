package cambio.simulator.orchestration.entities.kubernetes;

import java.util.List;

import cambio.simulator.entities.NamedEntity;
import desmoj.core.simulator.Model;
import lombok.Getter;

public class NodeList extends NamedEntity {
    @Getter
    private List<Node> nodes;

    public NodeList(Model model, String name, boolean showInTrace, List<Node> nodes) {
        super(model, name, showInTrace);
        this.nodes = nodes;
    }
}
