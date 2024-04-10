package cambio.simulator.orchestration.events;

import cambio.simulator.events.ExperimentAction;
import desmoj.core.simulator.Model;

public abstract class OrchestrationExperimentAction extends ExperimentAction {
    public OrchestrationExperimentAction(Model model, String s, boolean b) {
        super(model, s, b);
    }
}
