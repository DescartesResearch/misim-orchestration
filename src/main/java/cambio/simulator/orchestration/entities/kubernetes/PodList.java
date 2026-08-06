package cambio.simulator.orchestration.entities.kubernetes;

import java.util.List;

import cambio.simulator.entities.NamedEntity;
import desmoj.core.simulator.Model;
import lombok.Getter;

public class PodList extends NamedEntity {
    @Getter
    private List<String> pods;

    public PodList(Model model, String name, boolean showInTrace, List<String> pods) {
        super(model, name, showInTrace);
        this.pods = pods;
    }
}
